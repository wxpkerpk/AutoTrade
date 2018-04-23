package com.wx.autotrade

import MakeVectors.genVectors
import com.wx.autotrade.service.DataCollectService
import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.{Normalizer, PCA}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SparkSession


object TrainModel {

  def train(symbol:String,len:Int)={
    val sc = new SparkContext("local", "svm")
    val spark = SparkSession
      .builder()
      .getOrCreate()

    val vectors=genVectors(6)(getAnalysis =DataCollectService.getCoinAnaysis)(symbol,len)

    val dataSet=spark.createDataFrame(vectors).toDF("feature","buyLabel","sellLabel")
    //范数p-norm规范化
    val normalizer = new Normalizer()
      .setInputCol("features")
      .setOutputCol("normFeatures")
    val vectorNorm=normalizer.transform(dataSet)
    vectorNorm.select("normFeatures","buyLabel","sellLabel").map(row=>{
      val features=row.getAs[org.apache.spark.ml.linalg.Vector](0)
      val buyLabel=row.getAs[Double](1)
      val sellLabel=row.getAs[Double](2)
      (LabeledPoint.apply(buyLabel,Vectors.dense(features.toArray)),LabeledPoint.apply(sellLabel,Vectors.dense(features.toArray)))
    })


   // val pcaModel=new PCA().setK(4).f

  }

}
