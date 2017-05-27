package main.scala

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, Macros}
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by krishnaraghubanshi on 1/5/17.
 */
class Repository(implicit executionContext: ExecutionContext) extends Config {

  val driver1 = new reactivemongo.api.MongoDriver
  private val mongoConnection = (driver1).connection(List("localhost:27017"))
  private val mongoDatabase = mongoConnection(mongoDb)
  private val tokens: BSONCollection = mongoDatabase("tokens")
  implicit val tokenHandler = Macros.handler[Token]

  def insertToken(token: Token): Future[Either[Token,String]] = {
    tokens.insert(token).map(_ => Left(token)).recover{
      case e:Exception => Right(e.getMessage)
    }
  }

  def updateTokenByValue(value: String, token: Token): Future[Int] = tokens.update(BSONDocument("value" -> value),token).map(_.n)

  def deleteTokenByValue(value: String): Future[Int] = tokens.remove(BSONDocument("value" -> value)).map(_.n)

  def findValidTokenByValue(value: String): Future[Option[Token]] = tokens.find(BSONDocument("value" ->value,  "valuevalidTo" -> BSONDocument("$gt" -> System.currentTimeMillis()))).cursor[Token]().headOption

  def addMethodToValidTokenByValue(value: String, method: String): Future[Option[Token]] = {

    tokens.update(BSONDocument("value" -> value), BSONDocument("$addToSet" -> BSONDocument("authMethods" -> method))).flatMap{ lastError =>
      if (lastError.nModified > 0) findValidTokenByValue(value) else Future.successful(None)
    }
  }


}
