import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import main.scala.{LoginRequest, TokenManager, Token}
import org.scalatest._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/**
 * Created by krishnaraghubanshi on 1/13/17.
 */
  class TokenApiSpecs  extends WordSpec with Matchers with ScalatestRouteTest with TokenManager {
    var tokenId: Long = 0L

//   val route =      pathPrefix("tokens") {
//     post {
//       entity(as[LoginRequest]) { loginRequest =>
//         complete {
//           Created -> ""
//         }
//
//       }
//     }
//   }



  "Token Api" should {

      "create new Token" in {
//        val requestEntity = HttpEntity(MediaTypes.`application/json`, JsObject("identityId" -> JsNumber(123),
//          "authMethod" -> JsString("a")).toString())
        val jsonRequest = ByteString(
          s"""
             |{
             |    "identityId":123,
             |    "authMethod": "a"
             |}
        """.stripMargin)

        val postRequest = HttpRequest(
          HttpMethods.POST,
          uri = "/tokens",
          entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))
        postRequest ~> route ~> check {
          response._3 shouldBe a [Token]
          val t = responseAs[Token]
          tokenId = t.identityId
          response.status should be(StatusCodes.Created)

        }
      }

      "get Token By Id" in {
        Get("/tokens/"+tokenId) ~> route ~> check{
          response.status should be(StatusCodes.OK)
          response shouldBe a [Token]

        }
      }

      "delete Token" in {
        Delete("/tokens/"+tokenId) ~> route ~> check{
          response.status should be(StatusCodes.OK)
          Get("/tokens/"+tokenId) ~> route ~> check {
            response.status should be(StatusCodes.OK)
          }
        }
      }

    }

}
