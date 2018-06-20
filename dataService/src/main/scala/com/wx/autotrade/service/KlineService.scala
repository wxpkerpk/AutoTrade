package com.wx.autotrade.service
import java.util.Date

import com.wx.autotrade.restful.HttpUtilManager
import org.json4s._
import org.json4s.jackson.JsonMethods._
class KlineService {

}
object KlineService{
  val url="https://www.binance.com"
  implicit val formats = DefaultFormats


  def getKlines(symbol:String,types:String,start:Long)={

    val paramsUrl=s"symbol=$symbol&interval=$types&startTime=$start&limit=400"
    val client= HttpUtilManager.getInstance()
     val resultStr= client.requestHttpGet(url,"/api/v1/klines",paramsUrl)
      val result= parse(resultStr).extract[Array[Array[String]]]
    result
  }

  def getKlineBycounts(symbol:String,length:Int,types:String,date:Date)={
    val prefix=types.substring(1,2)
    val count=types.substring(0,1).toInt
    val countLen=prefix match {
      case "m"=>1*count
      case "h"=>60*count

    }
    val time=date.getTime
    var timeNow=time-length*countLen*60l*1000l
    val internals=length/400
    val restCount=length%400
    val result=new collection.mutable.ArrayBuffer[com.wx.autotrade.entity.Kline]()
    for(i <-0.until(internals)){
      val array=getKlines(symbol,s"${types}",timeNow)
     val lines= array.map{
        lines=>com.wx.autotrade.entity.Kline(new Date(lines(0).toLong),lines(1).toDouble,lines(4).toDouble,lines(2).toDouble,lines(3).toDouble,lines(5).toDouble)
      }
      result++=lines
      timeNow += (400*countLen*60*1000l)
    }
    val array=getKlines(symbol,s"${types}",timeNow)
    val lines= array.map{
      lines=>com.wx.autotrade.entity.Kline(new Date(lines(0).toLong),lines(1).toDouble,lines(4).toDouble,lines(2).toDouble,lines(3).toDouble,lines(5).toDouble)
    }
    result++=lines
    result.toArray


  }

  def main(args: Array[String]): Unit = {
  }




}
