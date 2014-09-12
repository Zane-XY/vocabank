package models

import java.sql.PreparedStatement

import anorm.Column.columnToArray
import anorm.SqlParser._
import anorm._
import anorm.jodatime.Extension._
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import play.api.Play.current
import play.api.db.DB

import scala.reflect.runtime.universe._
import scala.util.Try

case class Entry(
  id: Option[Long],
  headword: String,
  source: Option[String],
  context: String,
  added: DateTime,
  updated: DateTime,
  tags: Option[Array[String]],
  rating: Int,
  sound: Option[String],
  pronunciation: Option[String],
  wordClass:Option[String],
  userId: Long)

object Entry {

  def assemble(id: Option[Long], headword: String, source: Option[String], context: String, rating: Option[Int]):Entry =
    Entry(id, headword, source, context, new DateTime(), new DateTime(), None , rating.getOrElse(1), None, None, None,0L)

  def disassemble(e: Entry) = Some(e.id, e.headword, e.source, e.context, Some(e.rating))

  implicit object sqlArrayToStatement extends ToStatement[java.sql.Array] {
    def set(s: PreparedStatement, i: Int, n: java.sql.Array) = s.setArray(i, n)
  }

  implicit def scalaArrToSqlArr[T](arr: Array[T])(implicit tag: TypeTag[T], conn: java.sql.Connection):java.sql.Array = {
    arr match {
      case _ if typeOf[T] <:< typeOf[String] => conn.createArrayOf("varchar", arr.asInstanceOf[Array[AnyRef]])
    }
  }

  private val entryParser:RowParser[Entry] = {
        get[Option[Long]]("id") ~
        get[String]("headword") ~
        get[String]("context") ~
        get[DateTime]("added") ~
        get[DateTime]("updated") ~
        get[Option[Array[String]]]("tags") ~
        get[Int]("rating") ~
        get[Option[String]]("sound") ~
        get[Option[String]]("pronunciation") ~
        get[Option[String]]("word_class") ~
        get[Long]("user_id") map {
          case id ~ headword ~ context ~ added ~ updated ~ tags ~rating ~ sound ~ pronunciation ~ wordClass ~ userId =>
            Entry(id , headword , None ,  context , added , updated , tags, rating, sound, pronunciation, wordClass, userId)
        }
  }

  def mkWhere(conds:Traversable[String]) = if(conds.isEmpty) "" else conds.mkString(" WHERE ", " AND ", " ")

  def listPage(params:Map[String, Option[String]], rows: Int = 10):(List[Entry], Int, Int) = {

    val page = params("page").flatMap(p => Try(p.toInt).toOption).getOrElse(1)
    val tags = params("tags").map(_.split(","))
    val userId = params("userId").map(_.toLong)

    DB.withConnection { implicit connection =>
      val wheres = Map("tags @> ({tags}::varchar[])" -> tags,
                       "user_id = {userId}" -> userId)

      val whereClause = mkWhere(wheres.filter(!_._2.isEmpty).keys)
      //val tagParamValue = tags.map(arr => connection.createArrayOf("varchar", arr.asInstanceOf[Array[AnyRef]]))
      val tagParamValue = tags.map(arr => scalaArrToSqlArr(arr))

      val r = SQL("SELECT * FROM entries " + whereClause + " ORDER BY added DESC LIMIT {rows} OFFSET {offset}")
        .on('rows -> rows)
        .on('userId -> userId)
        .on('offset -> (if (page >= 1) page - 1 else 0) * rows)
        .on('rows -> rows)
        .on('tags -> tagParamValue)
        .as(entryParser *)


      val t = SQL("SELECT COUNT(*) FROM entries" + whereClause)
        .on('tags -> tagParamValue)
        .on('userId -> userId)
        .as(scalar[Long].single)


      (r, (t.toFloat / rows).ceil.toInt, page)
    }
  }

  def loadTags:collection.mutable.Set[String] = {
    DB.withConnection{ implicit conn =>
      val r = SQL("SELECT distinct unnest(tags) as all_tags FROM entries").as(get[String]("all_tags") *)

      collection.mutable.HashSet(r:_*)
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

  def updateTags(id: Long, value: Array[String], userId:Long):Int = {
    DB.withConnection { implicit conn =>
      SQL("UPDATE entries SET tags = {value} WHERE id = {id} AND user_id = {userId}")
        .on('value -> conn.createArrayOf("varchar", value.asInstanceOf[Array[AnyRef]]) , 'id -> id, 'userId -> userId).executeUpdate
    }
  }

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
          .replaceAll("BrE|AmE|NAmE","")
          .replaceAll("""<img src="qrcx:.+?>""","")
  }

  def cleanDOM(s:String): String = {
    val doc = Jsoup.parseBodyFragment(s);
    val nested = doc.select("span.dsl_ex > div")
    if(nested.isEmpty) s else {
      nested.unwrap()
      doc.body().children().toString
    }
  }

  def sanitize(e: Entry): Entry = {
    val whiteList = Whitelist.basic()
      .addTags("div", "span", "font", "img")
      .addAttributes("span", "style")
      .addAttributes("img", "src")
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
