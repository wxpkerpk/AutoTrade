package com.wx.algorithm
import com.wx.autotrade.entity.Analysis
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

object MakeVectors{

  def genVectors(assLen:Int)(getAnalysis:(String,Int)=>Array[Analysis])(symbol:String,len:Int)={
    val arrayAnalysis=getAnalysis(symbol,len)
    val index=assLen-1
    val featuresArray=for(i<- index until arrayAnalysis.length) yield {
      val features=for(j<- 0 until assLen) yield {
        val data= arrayAnalysis(i+ j-assLen+1)
        data.dPrice
      }
      (features.toArray,arrayAnalysis(i).buy,arrayAnalysis(i).sell)
    }
    val buyVectors=featuresArray.map(x=>LabeledPoint(x._2,Vectors.dense(x._1)))
    val sellVectors=featuresArray.map(x=>LabeledPoint(x._3,Vectors.dense(x._1)))
    (buyVectors,sellVectors)
  }




}