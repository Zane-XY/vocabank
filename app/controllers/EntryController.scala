package controllers

import controllers.Common._
import models._
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF.Token._
import play.filters.csrf._
import security.Secured
import utils.SoundScraper

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object EntryController extends Controller with Secured {

  val entryF = Form(
    mapping(
      "id" -> optional(longNumber),
      "headword" -> nonEmptyText,
      "source" -> optional(text),
      "context" -> text,
      "tags" -> optional(text),
      "rating" -> optional(number(min = 1, max = 5))
    )(Entry.assemble)(Entry.disassemble))

  def entries = Action { implicit req =>
    val page = req.getQueryString("p").flatMap(p => Try(p.toInt).toOption).getOrElse(1)
    val (entries, total) = Entry.listPage(userIdFromSession, if (page >= 1) page - 1 else 0)
    Ok(views.html.entries(entries, total, page))
  }

  def delete = CSRFCheck {
    Action(parse.json) { implicit req =>
      import controllers.JsonValidators.entryIdReads
      userIdFromSession.fold(NotSignedIn)(userId =>
        req.body.validate[Long].map {
          case id => Ok(Json.obj("status" -> "OK", "rows deleted" -> Entry.delete(id, userId)))
        }.recoverTotal(BadRequestJSON)
      )
    }
  }

  /**
   * take strict JSON format, jquery should pass stringfied json object.
   * @return
   */
  def setRating = CSRFCheck {
    Action(parse.json) { implicit req =>
      import controllers.JsonValidators.ratingReads
      userIdFromSession.fold(NotSignedIn)(userId =>
        req.body.validate[(Long, Int)].map {
          case (id, value) => Ok(Json.obj( "status" -> "OK", "updated" -> Entry.updateRating(id, value, userId)))
        }.recoverTotal(BadRequestJSON))
   }
  }

  def lookupTags = Action { implicit req =>
    Cache.getAs[mutable.HashSet[String]]("tags").fold(Ok("empty tags")){ tags =>
      req.getQueryString("q").fold(Ok(Json.toJson(tags))){ term =>
        Ok(Json.toJson(tags.filter(_.startsWith(term))))
      }
    }
  }

  def setTags = CSRFCheck {
    Action(parse.json) { implicit req =>
      import controllers.JsonValidators.tagsReads
      userIdFromSession.fold(NotSignedIn)(userId =>
        req.body.validate[(Long, String)].map {
          case (id, value) => {
            val tagsArr = value.split("\\s*,\\s*")
            Cache.getAs[mutable.HashSet[String]]("tags").map { tags =>
              Cache.set("tags", tags ++ tagsArr)
            }
            Ok(Json.obj("status" -> "OK", "updated" -> Entry.updateTags(id, tagsArr.mkString(",",",",","), userId)))
          }
        }.recoverTotal(BadRequestJSON))
    }
  }

  def setSound = Action(parse.json) { implicit req =>
      import controllers.JsonValidators.soundReads
        req.body.validate[(Long, String)].map {
          case (id, word) => {
            SoundScraper.scrape(word).fold(
              Ok(Json.obj("status" -> "KO"))
              ){ sound =>
                Future {
                  Entry.updateSound(id, sound)
                }
                Ok(Json.obj("status" -> "OK", "sound" -> sound))
               }
          }
        }.recoverTotal(BadRequestJSON)
   }

  /**
   * form post is filtered by CSRF
    * @return
   */
  def save = CSRFCheck {
    Action (parse.json) { implicit req =>
      userIdFromSession.fold(NotSignedIn)(userId =>
          entryF.bindFromRequest.fold(
            err => BadRequest(err.errorsAsJson),
            entry => {
              Entry.save(entry.copy(userId = userId))
              Ok(Json.obj("status" -> "OK"))}
          )
      )
    }
  }

  /**
   * new entry form submit
   * @return
   */
  def submit = CSRFCheck {
    Action { implicit req =>
      userIdFromSession.fold(NotSignedIn)(userId =>
        entryF.bindFromRequest.fold(
          err => BadRequest(views.html.errors(err.errors)),
          entry => {
            Entry.save(entry.copy(userId = userId))
            Redirect(routes.EntryController.entries)
          }
        )
      )
    }
  }

  def remoteSaveGet = Action { implicit req =>
    req.headers.get("Authorization").map { basicAuth =>
      (User.auth _).tupled(decodeBasicAuth(basicAuth)) match {
        case (Some(u), m) => Ok(Messages("info.user.auth.success"))
        case (_, m) => HTTPBasicAuthFailed
      }
    }.getOrElse(HTTPBasicAuthFailed)
  }

  def remoteSave = Action(parse.json) { implicit req =>
    req.headers.get("Authorization").map { basicAuth =>
      (User.auth _).tupled(decodeBasicAuth(basicAuth)) match {
        case (Some(u), m) =>
          entryF.bindFromRequest.fold(
            err => BadRequest(err.errorsAsJson),
            entry => {
              Entry.save(entry.copy(userId = u.id.get))
              Ok(Json.obj("status" -> "OK"))
            }
          )
        case (_, m) => Unauthorized
      }
    }.getOrElse(Unauthorized)
  }

}

object JsonValidators {
  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val ratingReads : Reads[(Long, Int)] = (
    (__ \ "id").read[Long] and
    (__ \ "value").read[Int](min(1) keepAnd max(5))
  ) tupled

  implicit val tagsReads : Reads[(Long, String)] = (
    (__ \ "id").read[Long] and
    (__ \ "value").read[String]
    ) tupled

  implicit val soundReads : Reads[(Long, String)] = (
    (__ \ "id").read[Long] and
    (__ \ "word").read[String]
    ) tupled

  implicit  val entryReads:Reads[Entry] = (
      (__ \ "id").read[Option[Long]] and
      (__ \ "headword").read[String] and
      (__ \ "source").read[Option[String]] and
      (__ \ "context").read[String] and
      (__ \ "tags").read[Option[String]] and
      (__ \ "rating").read[Option[Int]]
    )(Entry.assemble _)


  implicit val entryIdReads : Reads[Long] = (__ \ "id").read[Long]

}
