package com.wx.autotrade.service

import com.wx.autotrade.restful.stock.impl.StockRestApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class StockClientService {

  @Value("${okex.api.rest.url}")
  var url:String= _
  @Value("${private_key}")
  var privateKey:String= _
  @Value("${okex.api.api_key}")
  var publicKey:String= _


  def getClient= new StockRestApi(publicKey,privateKey,url)



}
