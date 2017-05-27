package controllers

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, AddRoutee, RemoveRoutee}
import akka.stream.ActorMaterializer
import flow.RouterName
import metrics.common.{Counter, Metric, Value}
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.{MessageFlowTransformer}
import play.api.mvc._

class MetricsCollector extends Controller {
  implicit val valueJsonFormat = Json.format[Value]
  implicit val counterJsonFormat = Json.format[Counter]
  implicit val metricJsonFormat = new Format[Metric] {
    override def reads(json: JsValue): JsResult[Metric] = ??? // not needed
    override def writes(o: Metric): JsValue = o match {
      case c: Counter => counterJsonFormat.writes(c)
      case v: Value => valueJsonFormat.writes(v)
    }
  }
  implicit val formatter = MessageFlowTransformer.jsonMessageFlowTransformer[String, Metric]

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  def index() =
  WebSocket.accept[String,Metric]{request => ActorFlow.actorRef(out => WebSocketHandlerActor.props(out))}
}

object WebSocketHandlerActor {
  def props(out: ActorRef) = Props(new WebSocketHandlerActor(out))
}

class WebSocketHandlerActor(out: ActorRef) extends Actor with RouterName{
  override def preStart(): Unit = {
     router ! AddRoutee(routee)
  }

  override def postStop(): Unit = {
    router ! RemoveRoutee(routee)
  }

  override def receive: Receive = {
    case m: Metric => out ! m
  }

  private val routee = ActorRefRoutee(self)

  private val router = context.system.actorSelection(RouterPath)
}
