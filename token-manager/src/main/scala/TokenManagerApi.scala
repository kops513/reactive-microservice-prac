package main.scala


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import metrics.common.Metrics

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

/**
 * Created by krishnaraghubanshi on 2/3/17.
 */


object TokenManagerApi extends  App with TokenManager{
  protected implicit val actorSystem: ActorSystem = ActorSystem("system")

  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  val t = Http().bindAndHandle(route, interface,  port)
  println(s"Server online at http://localhost:8010/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses retur
  t.flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate())
}