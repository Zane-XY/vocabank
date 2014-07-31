package models

import anorm._
import anorm.jodatime.Extension._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

import org.joda.time.{LocalDateTime, DateTime}

case class Entry(
  id: Option[Long],
  headword: String,
  source: Option[List[String]],
  context: String,
  added: DateTime,
  updated: DateTime,
  tags: Option[List[String]],
  rating: Int,
  userId: Long)

object Entry {

  def assemble(id: Option[Long], headword: String, source: Option[List[String]], context: String, tags: Option[List[String]], rating: Int):Entry =
    Entry(id, headword, source, context, new DateTime(), new DateTime(), tags, rating, 0L)

  def disassemble(e: Entry) = Some(e.id, e.headword, e.source, e.context, e.tags, e.rating)

  private val entryParser:RowParser[Entry] = {
        get[Option[Long]]("ID") ~
        get[String]("HEADWORD") ~
        get[String]("CONTEXT") ~
        get[DateTime]("ADDED") ~
        get[DateTime]("UPDATED") ~
        get[Int]("RATING") ~
        get[Long]("USER_ID") map { case id ~ headword ~ context ~ added ~ updated ~ rating ~ userId =>
            Entry(id , headword , None ,  context , added , updated , None, rating, userId) }
  }

  def listAll(userId: Long):List[Entry] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM ENTRIES WHERE USER_ID = {userId} ORDER BY ADDED DESC").on('userId -> userId).as(entryParser *)
    }
  }

  /**
   *
   * @param id
   * @param value
   * @return the counts of updated rows
   */
  def setRating(id: Long, value: Int, userId:Long):Int = {
    DB.withConnection { implicit connection =>
      SQL("""
         UPDATE ENTRIES SET RATING = {value} WHERE ID = {id} AND USER_ID = {userId} """).on(
          'value -> value, 'id -> id, 'userId -> userId).executeUpdate
    }
  }

  def delete(id:Long, userId:Long) = {
   DB.withConnection { implicit conn =>
    SQL(
      """
        DELETE FROM ENTRIES WHERE ID = {id} AND USER_ID = {userId}
      """.stripMargin).on('id -> id, 'userId -> userId).executeUpdate()
   }
  }

  def save(r: Entry) {
    DB.withConnection { implicit connection =>
      r.id.fold(
        SQL(
          """
            INSERT INTO ENTRIES ( HEADWORD, CONTEXT, ADDED, UPDATED, RATING, USER_ID)
            VALUES ( {headword}, {context}, {added}, {updated}, {rating}, {userId})
          """).on (
              'headword -> r.headword,
              'context -> r.context,
              'added -> r.added,
              'updated -> r.updated,
              'rating -> r.rating,
              'userId -> r.userId).executeUpdate
    )(id =>
       SQL(
        """
          UPDATE ENTRIES SET HEADWORD = {headword}, CONTEXT = {context}, UPDATED = {updated} WHERE ID = {id}
        """.stripMargin).on('headword -> r.headword, 'context -> r.context, 'updated -> new DateTime(), 'id -> id).executeUpdate()
      )
    }
  }
}
