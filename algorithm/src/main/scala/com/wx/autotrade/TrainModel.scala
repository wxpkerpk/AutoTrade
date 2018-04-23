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

  def train(symbol:String,len:Int)={
    val sc = new SparkContext("local", "svm")
    val spark = SparkSession
      .builder()
      .getOrCreate()

    import spark.implicits._
    val vectors=genVectors(7)(getAnalysis =DataCollectService.getCoinAnaysis)(symbol,len)

    val dataSet=spark.createDataFrame(vectors).toDF("feature","buyLabel","sellLabel")
    //范数p-norm规范化
    val normalizer = new Normalizer()
      .setInputCol("feature")
      .setOutputCol("normFeatures")
    val vectorNorm=normalizer.transform(dataSet)
    val vectorLabel=vectorNorm.select("normFeatures","buyLabel","sellLabel").map(row=>{
      val features=row.getAs[org.apache.spark.ml.linalg.Vector](0)
      val buyLabel=row.getAs[Double](1)
      val sellLabel=row.getAs[Double](2)
      (LabeledPoint.apply(buyLabel,Vectors.dense(features.toArray)),LabeledPoint.apply(sellLabel,Vectors.dense(features.toArray)))
    }).rdd

    val featuresVectors=vectorLabel.map(_._1).cache()
   val buyModel= SVMWithSGD.train(vectorLabel.map(_._1),10000,0.01,0.01,0.98)
  val result  =buyModel.predict(featuresVectors.map(_.features))

    val count=result.count()
   val rightCount= result.zip(featuresVectors).map(x=>{
     x._1==x._2.label
    }).filter(y=>y).count()

    println(s"模型准确率为： ${rightCount.toDouble/count.toDouble}")


  }

  def main(args: Array[String]): Unit = {
    train("BTCUSDT",10000)
  }

}
