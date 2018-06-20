package com.wx.autotrade.entity

import java.util.Date

case class Kline(date:Date,begin:Double,close:Double,max:Double,min:Double,vol:Double) extends Serializable{

  import java.text.DateFormat
  import java.text.SimpleDateFormat

  val format = new SimpleDateFormat("yyyy-MM-dd H:mm")
  override def toString: String = {
    s"${format.format(date)},$begin,$close,$max,$min,$vol,coin"
  }

}
object Kline{
  def makeHead()={
    "date,begin,close,max,min,vol,code"
  }

}
case class Analysis(date:Date,dPrice:Double,dMax:Double,dMin:Double, var dVol:Double,var buy:Double,var  sell:Double) extends Serializable
