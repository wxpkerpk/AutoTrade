import com.wx.autotrade.entity.Analysis
import org.apache.spark.mllib.linalg.Vectors

object MakeVectors{

  def genVectors(assLen:Int)(getAnalysis:_=>Array[Analysis])={
    val arrayAnalysis=getAnalysis()
    var i=assLen
    while(i<arrayAnalysis.length){




    }

  }




}