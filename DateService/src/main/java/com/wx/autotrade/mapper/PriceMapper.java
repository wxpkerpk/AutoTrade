package com.wx.autotrade.mapper;

import com.wx.autotrade.entity.Price;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
public interface PriceMapper {
    @Select("SELECT * FROM price WHERE name = #{name} and date >= #{time}")
    List<Price> getByNameAndTime(@Param(value = "name") String name,@Param(value = "time")Date time);
    @Insert("INSERT INTO price(name,date,maxPrice,minPrice,avgPrice,depth,intervals,openPrice,closePrice,id,vol) VALUES(#{name}, #{date}, #{maxPrice}, #{minPrice}, #{avgPrice}, #{depth}, #{intervals}, #{openPrice}, #{closePrice},#{id},#{vol})")
    void insert(Price price);
}
