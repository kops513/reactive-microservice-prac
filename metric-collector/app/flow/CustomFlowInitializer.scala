package flow

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshal}
import akka.routing.{AddRoutee, BroadcastGroup, NoRoutee, RemoveRoutee}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import metrics.common.{Metrics, Counter, Metric, Value}
import play.api.Application
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Akka
import play.api.mvc.WebSocket.MessageFlowTransformer
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.Macros
import reactivemongo.core.nodeset.Authenticate
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.collection.immutable.Seq
import scala.concurrent.Future
/**
 * Created by krishnaraghubanshi on 1/3/17.
 */

class CustomFlowInitializer @Inject() (val app: Application) extends RouterName{
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load()
  val interface = config.getString("metrics-collector.interface")
  val port = config.getInt("metrics-collector.port")

  val requestFlow = Flow.fromGraph(GraphDSL.create(){
     implicit builder =>
      import GraphDSL.Implicits._
       val broadcastRequest = builder.add(Broadcast[HttpRequest](2))
       val broadcastMetric = builder.add(Broadcast[Metric](2))
       implicit val metricsUM: FromRequestUnmarshaller[Metric] = ???

       val requestResponseFlow = builder.add(Flow[HttpRequest].map(_ => HttpResponse(akka.http.scaladsl.model.StatusCode.int2StatusCode(200))))
       val requestMetricFlow = builder.add(Flow[HttpRequest].mapAsync(1) { request =>
         Unmarshal(request.entity).to[Metric].map(Seq(_)).fallbackTo(Future.successful(Seq.empty[Metric]))
       }.mapConcat(identity(_)))

       val wsSubscriber = builder.add(Sink.actorSubscriber[Metric](ActorsWs.props))
       val journalerSubscriber = builder.add(Sink.actorSubscriber[Metric](ActorJournaler.props()))

       broadcastRequest ~> requestResponseFlow
       broadcastRequest ~> requestMetricFlow ~> broadcastMetric ~> wsSubscriber
                                                broadcastMetric ~> journalerSubscriber

       FlowShape(broadcastRequest.in, requestResponseFlow.outlet)


  })

  Http().bindAndHandle(requestFlow, interface, port )

  val router = system.actorOf(BroadcastGroup(List.empty[String]).props(), RouterName)
  router ! AddRoutee(NoRoutee) // prevents router from terminating when last websocket disconnects

}

trait RouterName {
  val RouterPath = s"/user/$RouterName"
  protected val RouterName = "BroadcastRouter"
}

class ActorsWs extends ActorSubscriber with RouterName{
  private val router = context.system.actorSelection(RouterPath)

  override def receive: Receive = {
    case OnNext(m: Metric) => router ! m
    case OnComplete => self ! PoisonPill
  }

  override protected def requestStrategy: RequestStrategy = new WatermarkRequestStrategy(1024)
}

object ActorsWs {
  def props: Props = Props(new ActorsWs)
}

class ActorJournaler extends ActorSubscriber    {

  implicit val counterMongoHandler = Macros.handler[Counter]
  implicit val valueMongoHandler = Macros.handler[Value]
  private implicit val dispatcher = context.dispatcher
  private val configuration = ConfigFactory.load()
  private val mongoHost = configuration.getString("mongo.host")
  private val mongoDb = configuration.getString("mongo.db")
  private val mongoUser = configuration.getString("mongo.user")
  private val mongoPassword = configuration.getString("mongo.password")
  val driver = new reactivemongo.api.MongoDriver
  private val mongoConnection = (driver).connection(List("localhost:27017"))
  private val mongoDatabase = mongoConnection(mongoDb)
   private val metrics: BSONCollection = mongoDatabase("metrics")

  override def receive: Receive = {
    case OnNext(c: Counter) => metrics.insert(c)
    case OnNext(v: Value) => metrics.insert(v)
    case OnComplete => self ! PoisonPill
  }

  override protected def requestStrategy: RequestStrategy = new WatermarkRequestStrategy(1024)
}

object ActorJournaler {
  def props(): Props = Props(new ActorJournaler())
}

class FlowStop @Inject() (lifecycle: ApplicationLifecycle, application: Application ) extends RouterName{
  val router = Akka.system(application).actorOf(BroadcastGroup(List.empty[String]).props(), RouterName)

  lifecycle.addStopHook { () =>
    Future.successful{
      router ! RemoveRoutee(NoRoutee)
      router ! PoisonPill}
  }
}