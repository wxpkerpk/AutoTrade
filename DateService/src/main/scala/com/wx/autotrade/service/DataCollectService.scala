package com.wx.autotrade.service

import java.util.{Date, UUID}

import akka.actor._
import akka.actor.{DeadLetter, Props}
import akka.event.Logging
import akka.actor.ActorSystem
import akka.util.Timeout
import com.wx.autotrade.entity.Price

import scala.concurrent.duration._
import com.wx.autotrade.mapper.PriceMapper
import com.wx.autotrade.start.SpringUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class Ticker(buy: Float, sell: Float, last: Float, vol: Double) {
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

  def caculatePrice(name:String,arrayBuffer: ArrayBuffer[Ticker])={
    val priceArray=arrayBuffer.map(_.last)
    val max=priceArray.max
    val min=priceArray.min
    val avg=priceArray.sum/priceArray.length
    val begin=priceArray.head
    val end=priceArray.takeRight(0).head

    val interval=(queueMaxLength*intervals)._1.toInt

    val price=new Price(UUID.randomUUID().toString,name,new Date(),max,min,avg,begin,end,"",1)


    price
  }
  def makeData(mapper: PriceMapper, clientService: StockClientService) = {
    import org.json4s.DefaultFormats._
    val client = clientService.getClient
    coinsType.foreach(name => {
      var success=false
      try {
        val str=client.ticker(name)
        val tickerJson = parse(str)
        val ticker = Ticker((tickerJson \ "buy").extract[Float],
          (tickerJson \ "sell").extract[Float],
          (tickerJson \ "last").extract[Float],
          (tickerJson \ "vol").extract[Double])
        coinsTickers.getOrElseUpdate("name", {
          new ArrayBuffer[Ticker]()
        }) += ticker
        success=true

      } catch {
        case e:Throwable  => println(s"${name}获取数据发生异常${e.toString}")

      }
      if (success&&coinsTickers("name").length>10) {
        val price=caculatePrice(name,coinsTickers("name"))
        val depth=client.depth(name)
        price.setDepth(depth)
        mapper.insert(price)
        coinsTickers("name").clear()
      }

    })

  }
}

object DataCollectService {
  val coinsType = new mutable.LinkedHashSet[String]

  val intervals=3 seconds
  val queueMaxLength=10
  val system = ActorSystem("autoTrade")
  val act1 = system.actorOf(Props[DataCollectService], "autoTrade")
  implicit val time = Timeout(5 seconds)



  def startCollectData() ={
    coinsType++=Array("btc_usdt","eos_usdt","btm_usdt")

    import scala.concurrent.ExecutionContext.Implicits.global
    val tick = "tick"
    val cancellable = system.scheduler.schedule( 10 seconds,intervals, act1, tick)

  }

}