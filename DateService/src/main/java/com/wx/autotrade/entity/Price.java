package com.wx.autotrade.entity;

import java.io.Serializable;
import java.util.Date;

public class Price  implements Serializable {
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String name;//币种名称
    Date date;//时间
    float maxPrice;//最大价格
    float minPrice;//最小价格
    float avgPrice;//平均价格
    int vol;
    float openPrice;//开盘价格
    float closePrice;//收盘价格
    String depth;//json数组 交易深度
    int intervals;//间隔，毫秒
    public float getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(float closePrice) {
        this.closePrice = closePrice;
    }

    public float getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(float openPrice) {
        this.openPrice = openPrice;
    }


    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public Price(String id, String name, Date date, float maxPrice, float minPrice, float avgPrice, float openPrice, float closePrice, String depth, int intervals) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.avgPrice = avgPrice;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.depth = depth;
        this.intervals = intervals;
    }

    public Price() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getIntervals() {
        return intervals;
    }

    public void setIntervals(int interval) {
        this.intervals = interval;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }

    public float getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(float avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }
}
