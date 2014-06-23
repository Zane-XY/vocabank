package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
/**
 * Created by znw on 6/23/14.
 */
case class Definition (id: Option[Long], entry: String, definition: String, format:String, source: String, entryId:Long)

object Definition {

private val rowParser: RowParser[Definition] = {
    get[Option[Long]]("id") ~
    get[String]("entry") ~
    get[String]("definition") ~
    get[String]("format") ~
    get[String]("source") ~
    get[Long]("entryId")  map {
      case id ~ entry ~ definition ~ format ~ source ~ entryId =>
        Definition(id, entry, definition, format, source, entryId)
    }
}

def save(r: Definition, entryId: Long) = {
  DB.withConnection { implicit connection =>
    SQL("""
            INSERT INTO DEFINITIONS(
                entry,
                definition,
                format,
                source,
                entryId
            ) VALUES (
                {entry},
                {definition},
                {format},
                {source},
                {entryId}
            )""").on(
      'entry -> r.entry,
      'definition -> r.definition,
      'format -> r.format,
      'source -> r.source,
      'entryId -> entryId).executeUpdate
  }
}

def selectByEntryId(entryId:Long) : List[Definition] = {
    DB.withConnection { implicit connection =>
        SQL("SELECT * FROM DEFINITIONS WHERE ENTRY_ID = {ENTRYID}")
            .on('entryId -> entryId).as(rowParser *)
    }
}



}
