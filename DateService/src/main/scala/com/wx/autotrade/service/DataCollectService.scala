package com.wx.autotrade.service

import java.io.PrintWriter
import java.util.{Date, UUID}

import akka.actor._
import akka.actor.{DeadLetter, Props}
import akka.event.Logging
import akka.actor.ActorSystem
import akka.util.Timeout
import com.wx.autotrade.entity.Price

import scala.concurrent.duration._
import com.wx.autotrade.mapper.PriceMapper
import com.wx.autotrade.restful.stock.impl.StockRestApi
import com.wx.autotrade.service.DataCollectService.time
import com.wx.autotrade.start.SpringUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._

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
  val system = ActorSystem("autoTrade")
  val act1 = system.actorOf(Props[DataCollectService], "autoTrade")
  implicit val time = Timeout(5 seconds)

  val filePath="C:\\"


  def startCollectData() = {
    coinsType ++= Array("btc_usdt", "eos_usdt", "btm_usdt")

    import scala.concurrent.ExecutionContext.Implicits.global
    val tick = "tick"
    val cancellable = system.scheduler.schedule(2 seconds, intervals, act1, tick)

  }

  def analysisData(len:Int)={
    coinsType.par.foreach(name=> {
      val source = Source.fromFile(s"$filePath$name.txt")
       val array=parse(source.mkString).extract[Array[Array[String]]]


    }


    }

  def getKlineData()={
    val intervals=20

    implicit val formats = DefaultFormats
    val url="https://www.okex.com"
    import java.io.PrintWriter
    val publicKey="ef63c6bb-463b-47dc-99d6-b016120fbd7d"
    val privateKey="6BA2A61B50CFFA00EA8B8F87C3612168"
    var time=System.currentTimeMillis()/1000-(2000*5*60)*intervals
    val client=new StockRestApi(url,publicKey,privateKey)
    coinsType.par.foreach(name=>{
      val out = new PrintWriter(s"$filePath$name.txt")


      val arrays=new ArrayBuffer[Array[String]]()
      for(i <- 0 until  intervals){
        val value=client.kline(name,"5min",2000,time)
        val array=parse(value).extract[Array[Array[String]]]
        time=time+2000* (intervals * 60 )

        array.foreach(x=>{
          arrays+=x
        })

      }
      import org.json4s.native.Serialization.{read, write}
      out.print(write(a = arrays.toArray))
      out.close()


    })


  }

  def main(args: Array[String]): Unit = {
    val intervals=20
    implicit val formats = DefaultFormats


    val coin_name=Array("btc_usdt","eos_usdt","btm_usdt")
    val url="https://www.okex.com"
      val publicKey="ef63c6bb-463b-47dc-99d6-b016120fbd7d"
        val privateKey="6BA2A61B50CFFA00EA8B8F87C3612168"







  }

}