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
  rating: Int)

object Entry {

    private val entryParser:RowParser[Entry] = {
        get[Option[Long]]("id") ~
        get[String]("title") ~
        get[Option[String]]("source") ~
        get[String]("content") ~
        get[DateTime]("added") ~
        get[DateTime]("updated") ~
        get[Option[String]]("tags") ~
        get[Int]("rating") map { case id ~ title ~ source ~ content ~ added ~ updated ~ tags ~ rating =>
            Entry(id , title , source.map(a => List(a)) , content , added , updated , tags.map(a => List(a)) , rating) }
    }

  def listAll():List[Entry] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from ENTRIES").as(entryParser *)
    }
  }

  def save(r: Entry) {
    DB.withConnection { implicit connection =>
      SQL("""
            INSERT INTO ENTRIES (
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
