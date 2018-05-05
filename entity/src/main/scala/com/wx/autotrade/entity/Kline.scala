package com.wx.autotrade.entity

import java.util.Date

case class Kline(date:Date,begin:Double,close:Double,max:Double,min:Double,vol:Double) extends Serializable
case class Analysis(date:Date,dPrice:Double,dMax:Double,dMin:Double, var dVol:Double,var buy:Double,var  sell:Double) extends Serializable
