

import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Created by kops513 on 12/5/16.
 */

case class AuthEntry(userIdentifier: String, identityId: Long, createdAt: Long, lastCard: Long)
//
case class Code(userIdentifier: String, cardIndex: Long, codeIndex: Long, code: String, createdAt: Long, activatedAt: Option[Long] = None, usedAt: Option[Long] = None)
//
class AuthEntries(tag: Tag) extends Table[AuthEntry](tag, "auth_entry"){
  def userIdentifier = column[String]("user_identifier", O.PrimaryKey)
  def identityId = column[Long]("identity_id")
  def createdAt = column[Long]("created_at")
  def lastCard = column[Long]("last_card")
  def *  = (userIdentifier, identityId, createdAt, lastCard)  <> (AuthEntry.tupled, AuthEntry.unapply)
}


class Codes(tag: Tag) extends Table[Code](tag, "code") {
  def userIdentifier = column[String]("user_identifier")
  def cardIndex = column[Long]("card_index")
  def codeIndex = column[Long]("code_index")
  def code = column[String]("code")
  def createdAt = column[Long]("created_at")
  def activatedAt = column[Option[Long]]("activated_at")
  def usedAt = column[Option[Long]]("used_at")
  def *  = (userIdentifier, cardIndex, codeIndex, code, createdAt, activatedAt, usedAt) <> (Code.tupled, Code.unapply)
}


class Repository(implicit executionContext: ExecutionContext)  {

  private val codesQuery = TableQuery[Codes]
  private val authEntriesQuery = TableQuery[AuthEntries]
  private val db  =  Database.forConfig("db")

  def useCode(userIdentifier: String, cardIdx: Long, codeIdx: Long, code: String): Future[Int] = {
      val query =  codesQuery.filter(codeQ => codeQ.userIdentifier === userIdentifier && codeQ.cardIndex === cardIdx &&
        codeQ.codeIndex === codeIdx ).map(_.usedAt).update(Some(System.currentTimeMillis))
//        codesQuery.filter( _. === userIdentifier &&
//          codeQ.cardIndex === cardIdx &&
//          codeQ.codeIndex === codeIdx &&
//          codeQ.code === code &&
//          codeQ.usedAt.isEmpty === true &&
//          codeQ.activatedAt >= (System.currentTimeMillis - codeActiveTime))
//          .map(_.usedAt).update(Some(System.currentTimeMillis))
      db.run(query)

  }
//
  def getIdentity(userIdentifier: String): Future[Option[Long]] = {
    val query = authEntriesQuery.filter(line => line.userIdentifier === userIdentifier).map(_.identityId).result.headOption
    val result = db.run(query)
  result
  }

  def getNextCardIndex(userIdentifier: String): Future[Long] = {
   val t = for {
      next  <- authEntriesQuery.filter(line => line.userIdentifier === userIdentifier).result.headOption
      query <- {
        next match {
          case Some(s) => authEntriesQuery.filter(line => line.userIdentifier === userIdentifier).map(_.lastCard).update(s.lastCard + 1)

        }

      }
   }yield(query)
    val c = db.run(t)
    c.map(_.toLong)
  }

  def saveAuthEntryAndCodeCard(authEntry: AuthEntry, codeCard: CodeCard) = {

       val query = for{
         _ <-  authEntriesQuery += authEntry
         _ <- {
           val codes = codeCard.codes.zipWithIndex.map{ case (code, idx) =>
             Code(codeCard.userIdentifier, codeCard.id, idx.toLong, code, System.currentTimeMillis())
           }
           codesQuery ++= codes

         }
       }yield()
       db.run(query)


  }

  def getInactiveCodesForUser(userIdentifier: String): Future[Seq[Code]] = {

     val query = codesQuery.filter(code => code.userIdentifier === userIdentifier && code.activatedAt.isEmpty === true).result
     val result = db.run(query)
     result
  }

  def activateCode(userIdentifier: String, cardIndex: Long, codeIndex: Long): Future[Int] = {

     val query =    codesQuery.filter(code => code.userIdentifier === userIdentifier &&
          code.cardIndex === cardIndex &&
          code.codeIndex === codeIndex).map(_.activatedAt).update(Some(System.currentTimeMillis))
    db.run(query)
  }


}
