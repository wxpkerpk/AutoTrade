package com.wx.autotrade.entity

import java.util.Date

case class Kline(date:Date,begin:Double,close:Double,max:Double,min:Double,vol:Double)
case class Analysis(date:Date,dPrice:Double, var dVol:Double)
