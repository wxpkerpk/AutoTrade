package com.wx.autotrade.service

import akka.actor._
import akka.actor.{Props,DeadLetter}
import akka.event.Logging
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import akka.pattern.gracefulStop
import scala.concurrent.{Await,Future}
class DateCollectService extends Actor{
  def receive = {
    case para:String => println(para)
    case _ => ()
  }
}
object DateCollectService{
  val system = ActorSystem("msSystem")
  val act1 = system.actorOf(Props[DateCollectService],"first")
  implicit val time = Timeout(5 seconds)
  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    //1.system.scheduler.scheduleOnce(2 seconds, act1, "foo")
    /*2.system.scheduler.scheduleOnce(2 seconds){
        act1 ? "Hello"
    }*/
    //3.这将会计划0ms后每50ms向tickActor发送 Tick-消息
    val Tick = "tick"
    val cancellable = system.scheduler.schedule(0 milliseconds,1 seconds,act1,Tick)
    //这会取消未来的Tick发送
    //cancellable.cancel()
  }

}