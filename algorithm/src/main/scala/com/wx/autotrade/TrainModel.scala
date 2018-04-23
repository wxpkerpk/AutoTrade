package com.wx.autotrade

import MakeVectors.genVectors
import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.{Normalizer, PCA}
import org.apache.spark.sql.SparkSession


object TrainModel {

  def train(symbol:String,len:Int)={
    val sc = new SparkContext("local", "svm")
    val spark = SparkSession
      .builder()
      .getOrCreate()

//    val vectors=genVectors(6)(getAnalysis =DataCollectService.getCoinAnaysis)(symbol,len)
//
//    val dataSet=spark.createDataFrame(vectors).toDF("feature","buyLabel","sellLabel")
//    //范数p-norm规范化
//    val normalizer = new Normalizer()
//      .setInputCol("features")
//      .setOutputCol("normFeatures")
//    normalizer.transform(dataSet)
   // val pcaModel=new PCA().setK(4).f

  }

}
