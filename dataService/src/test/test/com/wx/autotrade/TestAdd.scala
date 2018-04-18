package com.wx.autotrade

import java.util.Date

import com.wx.autotrade.entity.Price
import com.wx.autotrade.mapper.PriceMapper
import com.wx.autotrade.start.StartWebApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[StartWebApplication]))
class TestAdd  {

  @Autowired
  var mapper:PriceMapper=_


   @Test
  def add():Unit={
     val price=new Price
     price.setAvgPrice(1)
     price.setClosePrice(1)
     price.setDate(new Date)
     price.setIntervals(1)
     price.setMaxPrice(1)
     price.setMinPrice(1)
     price.setId("2")
     price.setDepth("1")
     price.setName("btc")
     //mapper.insert(price)
     val data=mapper.getByNameAndTime("1",new Date)
     data.forEach(println(_))






  }
}