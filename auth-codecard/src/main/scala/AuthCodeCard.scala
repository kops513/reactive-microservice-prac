import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import StatusCodes._

import scala.io.StdIn

//if we dont import this we get  could not find implicit value for parameter um: akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller[
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/**
 * Created by kops513 on 12/5/16.
 */
case class CodeCard(id: Long, codes: Seq[String], userIdentifier: String)
case class RegisterResponse(identity: Identity, codesCard: CodeCard)
case class LoginRequest(userIdentifier: String, cardIndex: Long, codeIndex: Long, code: String)
case class ActivateCodeRequest(userIdentifier: String)
case class ActivateCodeResponse(cardIndex: Long, codeIndex: Long)
case class GetCodeCardRequest(userIdentifier: String)
case class GetCodeCardResponse(userIdentifier: String, codesCard: CodeCard)


case class Identity(id: Long)
case class Token(value: String, validTo: Long, identityId: Long, authMethods: Set[String])

object AuthCodeCardCard extends App with JsonProtocols with Config {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val repository = new Repository
  val gateway = new Gateway
  val service = new Service(gateway, repository)

  val route =
  pathPrefix("codecard"){
    path("register"){
      post{
                headerValueByName("Auth-Token") { tokenValue =>
                  complete {
                    val token = if(tokenValue.isEmpty) None else Some(tokenValue)
                    service.register(token).map[ToResponseMarshallable] {
                              case Right(response) => Created -> response
                              case Left(errorMessage) => BadRequest -> errorMessage
                            }
                  }
                }
      }
    }~
    path("login" / "activate"){
        post{
          entity(as[ActivateCodeRequest]) {request =>
            complete {
              service.activateCode(request).map[ToResponseMarshallable] {
              case Right(response) => OK -> response
              case Left(errorMessage) => BadRequest -> errorMessage
            }
            }
          }
        }
    }~
    path("login"){
      pathEnd {
        post {
          headerValueByName("Auth-Token") { tokenValue =>
            entity(as[LoginRequest]) { request =>
              complete {
                val token = if(tokenValue.isEmpty) None else Some(tokenValue)
                service.login(request, token).map[ToResponseMarshallable] {
                  case Right(response) =>Created -> response
                  case Left(errorMessage) => BadRequest -> errorMessage
                }
              }
            }
          }
        }
      }

    }~
    path("generate")  {
      post {
        headerValueByName("Auth-Token") { tokenValue =>
          entity(as[GetCodeCardRequest]) { request =>
            complete {
              val token = if(tokenValue.isEmpty) None else Some(tokenValue)
              service.getCodeCard(request, token).map[ToResponseMarshallable] {
              case Right(response) => OK -> response
              case Left(errorMessage) => BadRequest -> errorMessage
              }
            }
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, interface, port)
  println(s"Server online at http://localhost:8005/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())

}
