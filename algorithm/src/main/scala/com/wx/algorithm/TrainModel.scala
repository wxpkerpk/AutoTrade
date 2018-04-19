package com.wx.algorithm
import MakeVectors.genVectors
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.PCA
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.classification.{ClassificationModel, SVMWithSGD}


object TrainModel {

  def train(symbol:String,len:Int)={
    val sc = new SparkContext("local", "svm")
    val vectors=genVectors(6)(getAnalysis = com.wx.autotrade.service.DataCollectService.getCoinAnaysis)(symbol,len)
    sc.parallelize(vectors._1.map(_.features))
   // val pcaModel=new PCA().setK(4).f

  }

}
