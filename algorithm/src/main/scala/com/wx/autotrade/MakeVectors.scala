package com.wx.autotrade

import com.wx.autotrade.entity.Analysis
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SparkSession

object MakeVectors{

  def genVectors(assLen:Int)(getAnalysis:(String,Int)=>Array[Analysis])(symbol:String,len:Int)={
    val arrayAnalysis=getAnalysis(symbol,len)
    val index=assLen-1
    val featuresArray=for(i<- index until arrayAnalysis.length) yield {
      val features=for(j<- 0 until assLen) yield {
        val data= arrayAnalysis(i+ j-assLen+1)
        data.dPrice
      }
      (Vectors.dense(features.toArray),arrayAnalysis(i).buy,arrayAnalysis(i).sell)
    }
    featuresArray.toSeq
  }

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext("local", "svm")
    val spark = SparkSession
      .builder()
      .getOrCreate()
    val dataFrame = spark.createDataFrame(Seq(
      (0, Vectors.dense(1.0, 0.5, -1.0)),
      (1, Vectors.dense(2.0, 1.0, 1.0)),
      (2, Vectors.dense(4.0, 10.0, 2.0))
    )).toDF("id", "features")

    // Normalize each Vector using $L^1$ norm.
    val normalizer = new Normalizer()
      .setInputCol("features")
      .setOutputCol("normFeatures")

    val l1NormData = normalizer.transform(dataFrame)
    println("Normalized using L^1 norm")
    val list=l1NormData.collect().map(_.toString())
    l1NormData.show()

    // Normalize each Vector using $L^\infty$ norm.
    val lInfNormData = normalizer.transform(dataFrame, normalizer.p -> Double.PositiveInfinity)
    println("Normalized using L^inf norm")
    lInfNormData.show()
  }




}