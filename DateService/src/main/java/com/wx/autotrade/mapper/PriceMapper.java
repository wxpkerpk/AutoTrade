package com.wx.autotrade.mapper;

import com.wx.autotrade.entity.Price;
import org.apache.ibatis.annotations.*;

import java.util.List;
public interface PriceMapper {
    @Select("SELECT * FROM price WHERE name = #{name} and date >= time")
    List<Price> getByNameAndTime(String name,long time);
    @Insert("INSERT INTO price(name,date,maxPrice,minPrice,avgPrice,depth,interval,openPrice,closePrice) VALUES(#{name}, #{date}, #{maxPrice},#{minPrice}, #{avgPrice}, #{depth}, #{interval}, #{openPrice}, #{closePrice})")
    void insert(Price price);
}
