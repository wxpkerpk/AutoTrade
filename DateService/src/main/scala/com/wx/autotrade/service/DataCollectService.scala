package com.wx.autotrade.service

import akka.actor._
import akka.actor.{DeadLetter, Props}
import akka.event.Logging
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import com.wx.autotrade.mapper.PriceMapper
import com.wx.utils.SpringUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable
case class Ticker(buy:Float,sell:Float,last:Float, vol:Double){
  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case otherTicker: Ticker =>   buy == otherTicker.buy && sell == otherTicker.sell && last == otherTicker.last && vol == otherTicker.vol
      case _ =>   super.equals(obj)

    }
  }


}
class DataCollectService extends Actor{
  var mapper:PriceMapper=_
  var clientService:StockClientService=_
  val coinsType= new mutable.LinkedHashSet[String]
  val coinsTickers= new mutable.LinkedHashMap[String,mutable.ArrayBuffer[Ticker]]

  def receive = {
    case para:String => {
      if(mapper==null) mapper= SpringUtils.getBean("priceMapper").asInstanceOf[PriceMapper]
      if(clientService==null) clientService=SpringUtils.getBean("stockClientService").asInstanceOf[StockClientService]
      makeData(mapper,clientService)
    }
    case _ => ()
  }

  def makeData(mapper:PriceMapper,clientService:StockClientService)={

    coinsType.foreach(name=>{
      val client=clientService.getClient
      val depthStr= client.depth(name)
      val tickerJson=parse(client.ticker(name))
      val ticker=Ticker((tickerJson \"buy").extract[Float],
        (tickerJson \"sell").extract[Float],
        (tickerJson \"last").extract[Float],
        (tickerJson \"vol").extract[Double])




    })

  }
}
object DataCollectService{
  val system = ActorSystem("msSystem")
  val act1 = system.actorOf(Props[DataCollectService],"first")
  implicit val time = Timeout(5 seconds)
  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    //1.system.scheduler.scheduleOnce(2 seconds, act1, "foo")
    /*2.system.scheduler.scheduleOnce(2 seconds){
        act1 ? "Hello"
    }*/
    //3.这将会计划0ms后每50ms向tickActor发送 Tick-消息
    val Tick = "tick"
    val cancellable = system.scheduler.schedule(0 milliseconds,1 seconds,act1,Tick)
    //这会取消未来的Tick发送
    //cancellable.cancel()
  }



}