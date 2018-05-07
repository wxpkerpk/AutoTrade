package com.wx.autotrade

import MakeVectors.genVectors
import com.wx.autotrade.service.DataCollectService
import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.{Normalizer, PCA}
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SparkSession


object TrainModel {

  def train(symbol: String, len: Int) = {
    val sc = new SparkContext("local", "svm")
    val spark = SparkSession
      .builder()
      .getOrCreate()

    import spark.implicits._
    val vectors = genVectors(6)(getAnalysis = DataCollectService.getCoinAnaysis)(symbol, len)

    val klines=DataCollectService.getKlineData(symbol,len)
    val dataSet = spark.createDataFrame(vectors).toDF("feature", "buyLabel", "sellLabel")
    //范数p-norm规范化
    val normalizer = new Normalizer()
      .setInputCol("feature")
      .setOutputCol("normFeatures")
    val vectorNorm = normalizer.transform(dataSet)
    val vectorLabel = vectorNorm.select("normFeatures", "buyLabel", "sellLabel").map(row => {
      val features = row.getAs[org.apache.spark.ml.linalg.Vector](0)
      val buyLabel = row.getAs[Double](1)
      val sellLabel = row.getAs[Double](2)
      (LabeledPoint.apply(buyLabel, Vectors.dense(features.toArray)), LabeledPoint.apply(sellLabel, Vectors.dense(features.toArray)))
    }).rdd

    val featuresVectors = vectorLabel.map(_._1).cache()
    val buyModel = SVMWithSGD.train(vectorLabel.map(_._1), 50000, 0.001, 0.01, 0.5)
    val sellModel=SVMWithSGD.train(vectorLabel.map(_._2), 50000, 0.001, 0.01, 0.5)
    val resultBuy = buyModel.predict(featuresVectors.map(_.features)).collect()
    val resultSell = sellModel.predict(featuresVectors.map(_.features)).collect()
    var haveMoney=true
    var add=1.0
    var buyPrice=0.0
    var sellPrice=0.0
    resultBuy.indices.foreach(x=>{
      if(resultBuy(x)!=resultSell(x)){
        if(resultBuy(x)==1&&haveMoney){
          buyPrice=klines(x).close
          haveMoney=false
        }else if(resultSell(x)==1&& !haveMoney){
          haveMoney=true
          sellPrice=klines(x).close
          add=add*(sellPrice/buyPrice)

        }
      }

    })
    println(s"收益:$add")




   // println(s"模型准确率为： ${rightCount.toDouble / count.toDouble}")


  }

  def main(args: Array[String]): Unit = {
    train("BTCUSDT", 10000)
  }

}
