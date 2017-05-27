package main.scala

import java.math.BigInteger
import java.security.SecureRandom

import akka.actor.ActorSystem
import akka.event.Logging
import org.slf4j.{LoggerFactory, Logger}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by krishnaraghubanshi on 1/5/17.
 */
class Service (repository: Repository)(implicit ec: ExecutionContext) extends Config{

  private val random = new SecureRandom()

  private val logger = LoggerFactory.getLogger(classOf[Service])

  private def refreshToken(token: Token): Token = token.copy(validTo = math.max(token.validTo, System.currentTimeMillis() + sessionTtl))

  private def generateToken: String = new BigInteger(255, random).toString(32)

  def login(loginRequest: LoginRequest): Future[Either[Token,String]] = {
    val newToken = createFreshToken(loginRequest.identityId, loginRequest.authMethod)
   repository.insertToken(newToken)

  }

  def relogin(reloginRequest: ReloginRequest): Future[Option[Token]] = {
    repository.addMethodToValidTokenByValue(reloginRequest.tokenValue, reloginRequest.authMethod)
  }

  def findAndRefreshToken(tokenValue: String): Future[Option[Token]] = {
    repository.findValidTokenByValue(tokenValue).map { tokenOption =>
      tokenOption.map { token =>
        val newToken = refreshToken(token)
        if (newToken != token)
          repository.updateTokenByValue(token.value, newToken).onFailure { case t => logger.error( "Token refreshment failed") }
        newToken
      }
    }
  }

  def logout(tokenValue: String): Unit = {
    repository.deleteTokenByValue(tokenValue).onFailure { case t => logger.error( "Token deletion failed") }
  }

  private def createFreshToken(identityId: Long, authMethod: String): Token = {
    Token(generateToken, System.currentTimeMillis() + tokenTtl, identityId, Set(authMethod))
  }

}
