package com.wx.autotrade.service

import java.io._
import java.util.{Date, UUID}

import akka.actor._
import akka.actor.{DeadLetter, Props}
import akka.event.Logging
import akka.actor.ActorSystem
import akka.util.Timeout
import com.wx.autotrade.entity.{Analysis, Kline, Price}

import scala.concurrent.duration._
import com.wx.autotrade.mapper.PriceMapper
import com.wx.autotrade.restful.stock.impl.StockRestApi
import com.wx.autotrade.service.DataCollectService.time
import com.wx.autotrade.start.SpringUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization.write

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.io.Source

case class Ticker(buy: Double, sell: Double, last: Double, vol: Double) {
  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case otherTicker: Ticker => buy == otherTicker.buy && sell == otherTicker.sell && last == otherTicker.last && vol == otherTicker.vol
      case _ => super.equals(obj)

    }
  }


}

class DataCollectService extends Actor {
  import DataCollectService.intervals
  import DataCollectService.queueMaxLength
  import DataCollectService.coinsType
  implicit val formats = DefaultFormats

  var mapper: PriceMapper = _
  var clientService: StockClientService = _
  val coinsTickers = new mutable.LinkedHashMap[String, mutable.ArrayBuffer[Ticker]]

  def receive = {
    case para: String => {
      if (mapper == null) mapper = SpringUtils.getBean(classOf[PriceMapper])
      if (clientService == null) clientService = SpringUtils.getBean(classOf[StockClientService])
      makeData(mapper, clientService)
    }
    case _ => ()
  }

  def caculatePrice(name:String,arrayBuffer: Array[Ticker])={
    val priceArray=arrayBuffer.map(_.last)
    val max=priceArray.max
    val min=priceArray.min
    val avg=priceArray.sum/priceArray.length
    val begin=priceArray.head
    val end=priceArray(priceArray.length-1)
    val price=new Price(UUID.randomUUID().toString,name,new Date(),max.toFloat,min.toFloat,avg.toFloat,
      begin.toFloat,end.toFloat,"",
      (intervals._1*priceArray.length).toInt)


    price.setVol(arrayBuffer(arrayBuffer.length-1).vol.toInt)
    price
  }
  def makeData(mapper: PriceMapper, clientService: StockClientService) = {
    import org.json4s.DefaultFormats._
    val client = clientService.getClient
    coinsType.foreach(name => {
      var success=false
      try {
        val str=client.ticker(name)
        val tickerJson = parse(str) \ "ticker"
        val ticker = Ticker((tickerJson \ "buy").extract[String].toFloat,
          (tickerJson \ "sell").extract[String].toFloat,
          (tickerJson \ "last").extract[String].toFloat,
          (tickerJson \ "vol").extract[String].toFloat)
        coinsTickers.getOrElseUpdate(name, {
          new ArrayBuffer[Ticker]()
        }) += ticker
        success=true

      } catch {
        case e:Throwable  => println(s"${name}获取数据发生异常: ${e.toString}")

      }
      if (success&&coinsTickers(name).length>=10) {
        val price=caculatePrice(name,coinsTickers(name).toArray)
        coinsTickers(name).clear()

        val depth=parse(client.depth(name))
        val ask=(depth \ "asks").extract[Array[Array[Float]]].map(_(1)).sum
        val bids=(depth \ "bids").extract[Array[Array[Float]]].map(_(1)).sum
        val depthStatics=s"{ask:$ask,bids:$bids}"

        price.setDepth(depthStatics)
        mapper.insert(price)

      }

    })

  }
}

object DataCollectService {
  val coinsType = new mutable.LinkedHashSet[String]

  val intervals=3 seconds
  val queueMaxLength=20
//  val system = ActorSystem("autoTrade")
  //val act1 = system.actorOf(Props[DataCollectService], "autoTrade")
  implicit val time = Timeout(5 seconds)

  val filePath="C:\\"


  def startCollectData() = {
    coinsType ++= Array("btc_usdt", "eos_usdt", "btm_usdt")

    import scala.concurrent.ExecutionContext.Implicits.global
    val tick = "tick"
   // val cancellable = system.scheduler.schedule(2 seconds, intervals, act1, tick)

  }


  def judgeBuyPrice(currentPrice:Double,futurePrices:Array[Double])={

    val len=futurePrices.count(x => {
      x > currentPrice
    })

    if(len==futurePrices.length && futurePrices(futurePrices.length-1)>currentPrice*1.003) true
    else false
  }
  def judgeSellPrice(currentPrice:Double,futurePrices:Array[Double])={

    val len=futurePrices.count(x => {
      x <= currentPrice
    })

    if(len==futurePrices.length && futurePrices(futurePrices.length-1)<currentPrice*1.003) true
    else false
  }

  def analysisData(array:Array[Kline])={
    implicit val formats = DefaultFormats
      val analysisArray=array.map{
        x=>
          Analysis(x.date,(x.close-x.begin)/x.begin,x.vol,(x.min-x.begin)/x.begin,(x.max-x.close)/x.begin,0,0)
      }
      analysisArray.indices.foreach(index=>{
        if(index>0) analysisArray(index).dVol=(analysisArray(index).dVol-analysisArray(index-1).dVol)/analysisArray(index).dVol
        if(index<=analysisArray.length-3) analysisArray(index).buy= if(judgeBuyPrice(array(index).close,Array(array(index+1).close,array(index+2).close))) 1 else 0
        if(index<=analysisArray.length-3) analysisArray(index).sell= if(judgeSellPrice(array(index).close,Array(array(index+1).close,array(index+2).close))) 1 else 0

        //println(analysisArray(index).date)
      })
      analysisArray(0).dVol=0
      val result=analysisArray
      //val out = new PrintWriter(s"$filePath${name}_analysis.txt")
      //out.print(write(a = result))
      //out.close()
      result





    }

  def serialize[T](o: T)(path:String) {
    val bos = new FileOutputStream(path)//基于磁盘文件流的序列化
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(o)
    oos.close()
  }


  /** Deserialize an object using Java serizlization */
  //  def deserialize[T](bytes: Array[Byte]): T = {
  //    val bis = new ByteArrayInputStream(bytes)
  //    val ois = new ObjectInputStream(bis)
  //    ois.readObject.asInstanceOf[T]
  //  }

  def deserialize[T](path:String): T = {
    val bis = new FileInputStream(path)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

  import org.json4s.JsonDSL._

  def getKlineData(symbol:String,len:Int,fromCache:Boolean=true,types:Int=5)={
    import KlineService.getKlineBycounts
    val intervals=1
    implicit val formats = DefaultFormats
    val path=symbol+len
    val file=new File(path)
    var arrays:Array[Kline]=null

    if(file.exists()&&fromCache){
      arrays= deserialize[Array[Kline]](path)

    }else{
       arrays=getKlineBycounts(symbol,len,types=types)
      serialize(arrays)(path)

    }
    arrays
  }
  def getCoinAnaysis(symbol:String,len:Int,types:Int=5)={
    val klineArray=getKlineData(symbol,len,true)
    //anaysis
  }

  def decionPrice(klines:Array[Kline],price:Array[Double],beginIndex:Int)={
      val len=price.length
    for(i<- 0 until len) yield {

    }

  }
  def main(args: Array[String]): Unit = {

    val symbol="EOS"+"BTC"
    val types=15
    val len=10000
    val kline_btc=getKlineData("BTCUSDT",len,false,types =types)
    val klinesymbol=getKlineData(symbol,len,false,types =types)
   val klines= klinesymbol.zip(kline_btc).map{case (s,btc)=>{
      Kline(s.date,s.begin*btc.begin,s.close*btc.close,s.max*btc.max,s.min*btc.min,s.vol)
    }
    }

    val print=new PrintWriter(s"C:\\Users\\wx\\Desktop\\coinslstm\\${symbol}_$types.csv")

    print.println(Kline.makeHead())
    klines.foreach(u=>{
      print.println(u.toString)
    })
    print.close()

    var buyPrice=0.0
    var sellPrice=0.0
    var haveMoney=true
    var add=1.0
    var i=0
    var sum=0
    var makeCount=0d
      while(i<klines.indices.length){
        if(i<klines.length-2&&(klines(i).close-klines(i).begin)/klines(i).begin>=0.006&&(klines(i+1).close-klines(i+1).begin)/klines(i+1).begin>=0.007&&haveMoney){
          buyPrice=(klines(i+1).close+klines(i+2).begin)*1.001/2
          haveMoney=false
          sum+=1
        }
        else if(i<klines.length-2&&(klines(i).close-klines(i).begin)/klines(i).begin<= -0.001&& !haveMoney){

          sellPrice=(klines(i+1).begin+klines(i+1).close)*0.999/2
          haveMoney=true
          add=add*(sellPrice/buyPrice)
          makeCount=makeCount + (if(sellPrice>buyPrice) 1d else 0d)
          println(klines(i).date+" : sell:"+sellPrice+"  buy:"+buyPrice+" +%"+(sellPrice/buyPrice-1)*(100))

        }
        i=i+1
      }

    println(klines(klines.length-1).begin+"    "+klines(0).begin)

    println(s"币种：$symbol 策略收益率：%${(add-1)*100}  自然涨幅：%${(klines(klines.length-1).begin-klines(100).begin)/klines(0).begin*100}")
    println(makeCount/sum.toDouble*100)






  }

}