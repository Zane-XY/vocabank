package models

import anorm._
import anorm.jodatime.Extension._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

import org.joda.time.{LocalDateTime, DateTime}

case class Entry(
  id: Option[Long],
  title: String,
  source: Option[List[String]],
  content: String,
  added: DateTime,
  updated: DateTime,
  tags: Option[List[String]],
  rating: Int,
  userId: Long)

object Entry {

  def assemble(id: Option[Long], title: String, source: Option[List[String]], content: String, tags: Option[List[String]], rating: Int):Entry =
    Entry(id, title, source, content, new DateTime(), new DateTime(), tags, rating, 0L)

  def disassemble(e: Entry) = Some(e.id, e.title, e.source, e.content, e.tags, e.rating)

  private val entryParser:RowParser[Entry] = {
        get[Option[Long]]("ID") ~
        get[String]("TITLE") ~
        get[String]("CONTENT") ~
        get[DateTime]("ADDED") ~
        get[DateTime]("UPDATED") ~
        get[Int]("RATING") ~
        get[Long]("USER_ID") map { case id ~ title ~ content ~ added ~ updated ~ rating ~ userId =>
            Entry(id , title , None ,  content , added , updated , None, rating, userId) }
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
            INSERT INTO ENTRIES ( TITLE, CONTENT, ADDED, UPDATED, RATING, USER_ID)
            VALUES ( {title}, {content}, {added}, {updated}, {rating}, {userId})
          """).on (
              'title -> r.title,
              'content -> r.content,
              'added -> r.added,
              'updated -> r.updated,
              'rating -> r.rating,
              'userId -> r.userId).executeUpdate
    )(id =>
       SQL(
        """
          UPDATE ENTRIES SET TITLE = {title}, CONTENT = {content}, UPDATED = {updated} WHERE ID = {id}
        """.stripMargin).on('title -> r.title, 'content -> r.content, 'updated -> new DateTime(), 'id -> id).executeUpdate()
      )
    }
  }
}
