package main.scala

import spray.json.DefaultJsonProtocol

/**
 * Created by krishnaraghubanshi on 1/5/17.
 */
trait JsonProtocol extends DefaultJsonProtocol {
  protected implicit val tokenFormat = jsonFormat4(Token.apply)
  protected implicit val reloginRequestFormat = jsonFormat2(ReloginRequest.apply)
  protected implicit val loginRequestFormat = jsonFormat2(LoginRequest.apply)
}