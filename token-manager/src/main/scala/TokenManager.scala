package main.scala

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import metrics.common._
import Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


/**
 * Created by krishnaraghubanshi on 1/5/17.
 */
case class  LoginRequest(identityId: Long, authMethod: String)

case class ReloginRequest(tokenValue: String, authMethod: String)

case class Token(value: String, validTo: Long, identityId: Long, authMethods: Set[String])

trait TokenManager extends Metrics with JsonProtocol with Config  {

  val repository = new Repository
  val service = new Service( repository)
//  def putMetricForRequestResponse(requestStats: RequestResponseStats): Unit = {
//    val method = requestStats.request.method.name.toLowerCase
//    putMetric(Value(s"token-manager.$method.time", requestStats.time))
//  }
//(measureRequestResponse(putMetricForRequestResponse) & DebuggingDirectives.logRequestResult("token-manager"))
  val route = pathPrefix("tokens"){
       post{
         entity(as[LoginRequest]){ loginRequest =>
           complete{
             putMetric(Counter("token-manager.post", 1))
            service.login(loginRequest).map[ToResponseMarshallable]{
            t =>
             t match {
             case Left(r) => Created -> r
             case Right(r) => InternalServerError -> r
             }

            }
           }

         }
       }~
       patch{
        entity(as[ReloginRequest]){reLoginRequest =>
           complete{
            service.relogin(reLoginRequest).map[ToResponseMarshallable] {
              case Some(token) =>
//                putMetric(Counter("token-manager.patch", 1))
                OK -> token
              case None =>
//                putMetric(Counter("token-manager.patch", -1))
                NotFound -> "Token expired or not found"
            }
           }
        }
       }~
       path(Segment){tokenValue =>
         get{
             complete {
//                OK -> Token("sss", 299.toLong, 33.toLong, Set("dfd"))
              service.findAndRefreshToken(tokenValue).map[ToResponseMarshallable] {
                case Some(token) =>
//                  putMetric(Counter("token-manager.get", 1))
                  OK -> token
                case None =>
//                  putMetric(Counter("token-manager.get", -1))
                  NotFound -> "Token expired or not found"
              }
            }
         }~
         delete{
          complete {
              service.logout(tokenValue)
//              putMetric(Counter("token-manager.delete", 1))
              OK
            }
         }
       }

  }



}
