package models

import anorm._
import anorm.jodatime.Extension._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

import org.joda.time.DateTime

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

  def listAll():List[Entry] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM ENTRIES ORDER BY ADDED DESC").as(entryParser *)
    }
  }

  /**
   *
   * @param id
   * @param value
   * @return the counts of updated rows
   */
  def setRating(id: Long, value: Int):Int = {
    DB.withConnection { implicit connection =>
      SQL("""
         UPDATE ENTRIES SET RATING = {value} WHERE ID = {id} """).on(
          'value -> value, 'id -> id).executeUpdate
    }
  }

  def save(r: Entry) {
    DB.withConnection { implicit connection =>
      SQL("""
            INSERT INTO ENTRIES (
              TITLE, CONTENT, ADDED, UPDATED, RATING, USER_ID)
            VALUES (
              {title}, {content}, {added}, {updated}, {rating}, {userId})
          """).on(
        'title -> r.title,
        'content -> r.content,
        'added -> r.added,
        'updated -> r.updated,
        'rating -> r.rating,
        'userId -> r.userId).executeUpdate
    }
  }
}
