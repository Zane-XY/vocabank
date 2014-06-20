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
  source: List[String],
  content: String,
  added: DateTime,
  updated: DateTime,
  tags: List[String],
  rating: Int)

object Entry {
  def save(r: Entry) = {
    DB.withConnection { implicit connection =>
      SQL("""
            INSERT INTO entries (
                title,
                content,
                added,
                updated,
                rating
            ) VALUES (
                {title},
                {content},
                {added},
                {updated},
                {rating}
            ) """).on(
        'title -> r.title,
        'content -> r.content,
        'added -> r.content,
        'updated -> r.updated,
        'rating -> r.rating).executeUpdate
    }
  }
}
