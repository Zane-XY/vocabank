package models

import anorm._
import anorm.jodatime.Extension._
import anorm.SqlParser._
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import play.api.db.DB
import play.api.Play.current

import org.joda.time.{LocalDateTime, DateTime}

import scala.collection
import scala.collection.parallel.mutable

case class Entry(
  id: Option[Long],
  headword: String,
  source: Option[String],
  context: String,
  added: DateTime,
  updated: DateTime,
  tags: Option[String],
  rating: Int,
  sound: Option[String],
  pronunciation: Option[String],
  wordClass:Option[String],
  userId: Long)

object Entry {

  def assemble(id: Option[Long], headword: String, source: Option[String], context: String, tags: Option[String], rating: Option[Int]):Entry =
    Entry(id, headword, source, context, new DateTime(), new DateTime(), tags, rating.getOrElse(1), None, None, None,0L)

  def disassemble(e: Entry) = Some(e.id, e.headword, e.source, e.context, e.tags, Some(e.rating))

  private val entryParser:RowParser[Entry] = {
        get[Option[Long]]("id") ~
        get[String]("headword") ~
        get[String]("context") ~
        get[DateTime]("added") ~
        get[DateTime]("updated") ~
        get[Option[String]]("tags") ~
        get[Int]("rating") ~
        get[Option[String]]("sound") ~
        get[Option[String]]("pronunciation") ~
        get[Option[String]]("word_class") ~
        get[Long]("user_id") map {
          case id ~ headword ~ context ~ added ~ updated ~ tags ~rating ~ sound ~ pronunciation ~ wordClass ~ userId =>
            Entry(id , headword , None ,  context , added , updated , tags, rating, sound, pronunciation, wordClass, userId)
        }
  }

  /**
   * returns entries and total page
   * @param userId
   * @param offset
   * @param rows
   * @return
   */
  def listPage(userId: Option[Long], offset: Int = 0, rows: Int = 10):(List[Entry], Int) = {
    DB.withConnection { implicit connection =>
      val r = userId.fold(
        SQL("SELECT * FROM entries ORDER BY added DESC LIMIT {rows} OFFSET {offset}")
          .on('offset-> offset * rows)
          .on('rows -> rows)
          .as(entryParser *)
      ){ id =>
       SQL("SELECT * FROM entries WHERE user_id = {userId} ORDER BY added DESC LIMIT {rows} OFFSET {offset}")
        .on('userId -> id)
        .on('offset-> offset * rows)
        .on('rows -> rows)
        .as(entryParser *)
      }

      val t = userId.fold(
        SQL("SELECT COUNT(*) FROM entries").as(scalar[Long].single)
      ) { id =>
        SQL("SELECT COUNT(*) FROM entries WHERE user_id = {userId}").on('userId -> id).as(scalar[Long].single)
      }

      (r, (t.toFloat / rows).ceil.toInt)
    }
  }

  def loadTags:collection.mutable.HashSet[String] = {
    DB.withConnection{ implicit conn =>
      collection.mutable.HashSet(
        SQL("SELECT distinct regexp_split_to_table(entries.tags, E',') FROM entries").as(get[String](1) *): _*
      )
    }
  }

  def count(userId: Long):Int = {
    DB.withConnection { implicit connection =>
      SQL("SELECT COUNT(*) FROM entries WHERE user_id").on('userId -> userId).as(scalar[Int].single)
    }
  }

  def listAll(userId: Long):List[Entry] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM entries WHERE user_id = {userId} ORDER BY added DESC").on('userId -> userId).as(entryParser *)
    }
  }

  def updateColumn(id: Long, column:String, value:ParameterValue, userId:Long):Int = {
    DB.withConnection { implicit connection =>
      SQL(" UPDATE entries SET " +  column +  "  = {value} WHERE id = {id} AND user_id = {userId}")
        .on('value -> value, 'id -> id, 'userId -> userId).executeUpdate
    }
  }

  def updateColumnWithoutId(id: Long, column:String, value:ParameterValue):Int = {
    DB.withConnection { implicit connection =>
      SQL(" UPDATE entries SET " +  column +  "  = {value} WHERE id = {id}")
        .on('value -> value, 'id -> id).executeUpdate
    }
  }

  def updateRating(id: Long, value: Int, userId:Long):Int = updateColumn(id, "rating", value, userId)

  /**
   * take tags in ,tag, format
   * @param id
   * @param value
   * @param userId
   * @return
   */
  def updateTags(id: Long, value: String, userId:Long):Int = updateColumn(id, "tags", value, userId)

  def updateSound(id: Long, value: String):Int = updateColumnWithoutId(id, "sound", value)

  def delete(id:Long, userId:Long) = {
   DB.withConnection { implicit conn =>
    SQL("DELETE FROM entries WHERE id = {id} AND user_id = {userId}".stripMargin)
      .on('id -> id, 'userId -> userId).executeUpdate()
   }
  }

  /**
   * only keep color and font-weight
   * @param s
   * @return
   */
  def cleanStyle(s:String):String = {
    val keepR = """((?<!-)color|font-weight):.+?;""".r
    val styleR = """(?<=\sstyle=")(.+?)(?=")""".r
    styleR.replaceAllIn(s, m => keepR.findAllIn(m.group(0)).mkString)
          .replaceAll("<span>&nbsp;</span>","")
  }

  def cleanDOM(s:String): String = {
    val doc = Jsoup.parseBodyFragment(s);
    //val nested = doc.select("span.dsl_ex div")
    val nested = doc.select("div span div")
    if(nested.isEmpty) s else {
      nested.unwrap()
      doc.body().children().toString
    }
  }

  def sanitize(e: Entry): Entry = {
    val whiteList = Whitelist.basic()
      .addTags("div", "style")
      .addTags("span", "font")
      .addAttributes("span", "style")
      .addAttributes("p", "style")
      .addAttributes("font", "face", "color")

    val ctx = Jsoup.clean(cleanDOM(e.context), whiteList)
    e.copy(context = cleanStyle(ctx))
  }

  def save(e: Entry) {
    val r = sanitize(e)
    DB.withConnection { implicit connection =>
      r.id.fold(
        SQL("""
            INSERT INTO entries ( headword, context, added, updated, rating, user_id)
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
          UPDATE entries SET headword = {headword}, context = {context}, updated = {updated} WHERE ID = {id}
        """.stripMargin).on('headword -> r.headword, 'context -> r.context, 'updated -> new DateTime(), 'id -> id).executeUpdate()
      )
    }
  }
}
