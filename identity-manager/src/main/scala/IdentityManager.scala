

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent._
import scala.io.StdIn

/**
 * Created by kops513 on 12/5/16.
 */

case class Identity(id: Option[Long], createdAt: Long)//
//
class Identities(tag: Tag) extends Table[Identity](tag, "identity"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[Long]("created_at")
  def *  = (id.?, createdAt)  <> (Identity.tupled, Identity.unapply)
}

object IdentitiyManager  extends App with DefaultJsonProtocol {

  private val identityQuery = TableQuery[Identities]
  private val db  =  Database.forConfig("db")

  val config = ConfigFactory.load()
  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  implicit val identityFormat = jsonFormat2(Identity.apply)

  def getAllIdentities(): Future[Seq[Identity]] = {
    db.run(identityQuery.result)

  }

  def saveIdentity(identity: Identity): Future[Identity] = {

       db.run (identityQuery returning identityQuery.map(_.id) into ((_, id) => identity.copy(id = Option(id))) += identity)

  }

  val bindingFuture = Http().bindAndHandle(interface = interface, port = port, handler = {
    logRequestResult("identity-manager") {
      path("identities") {
        pathEndOrSingleSlash {
          post {
            complete {
              val newIdentity = Identity(id = None, createdAt = System.currentTimeMillis())
              Created -> saveIdentity(newIdentity)
            }
          } ~
            get {

              complete {
                getAllIdentities().map[ToResponseMarshallable](t => OK -> t.toSeq)
              }
            }
        }
      }
    }
  })
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())


}
