package controllers

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Message
import play.api.libs.Comet
import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import models._

import org.joda.time.DateTime

import play.filters.csrf._
import play.filters.csrf.CSRF.Token._
import utils.{OxfordLearnerScraper, LongmanContemporaryScraper, CambridgeLearnerScraper}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

//import play.api.libs.json._
//import play.api.libs.json.Reads._
//import play.api.libs.functional.syntax._

object EntryController extends Controller {

  def formToEntry(id: Option[Long], title: String, source: Option[List[String]], content: String, tags: Option[List[String]], rating: Int): Entry = {
    val now = new DateTime
    Entry(None, title, source, content, now, now, tags, rating, 0L)
  }

  def entryToForm(e: Entry) = Some( e.id, e.title, e.source, e.content, e.tags, e.rating)

  val entryF = Form(mapping(
    "id" -> optional(longNumber),
    "title" -> nonEmptyText,
    "source" -> optional(list(text)),
    "content" -> text,
    "tags" -> optional(list(text)),
    "rating" -> number(min = 1, max = 5))(formToEntry)(entryToForm))

  //entryF("rating").value

  def entries = Action {
    implicit request =>
    Ok(views.html.entries(Entry.listAll))
  }

  def add = Action { implicit request =>
    Ok(views.html.entry(entryF))
  }

  def delete(id:Long) = Action { implicit request =>
    Ok(views.html.entry(entryF))
  }

  def edit(id:Long) = Action { implicit request =>
    Ok(views.html.entry(entryF))
  }

  /**
   * take strict JSON format, jquery should pass stringfied json object.
   * @return
   */
  def setRating = CSRFCheck {
    Action(parse.json) { implicit req =>
      import JsonValidators.ratingReads
      req.body.validate[(Long, Int)].map {
        case (id, value) => Ok(Json.obj( "status" -> "OK", "updated" -> Entry.setRating(id, value)))
      }.recoverTotal{
        e => BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))
      }
   }
  }

  /**
   * form post is filtered by CSRF
    * @return
   */
  def save = CSRFCheck {
    Action (parse.json) { implicit req =>
      import JsonValidators.entryReads
      req.session.get("userId").flatMap(s => Try{s.toLong}.toOption).fold(
        BadRequest(Json.obj("status" -> "KO" , "error" -> Messages("error.user.not.signedIn")))
      )(
        userId =>
          entryF.bindFromRequest.fold(
            err => BadRequest(err.errorsAsJson),
            entry => {
              Entry.save(entry.copy(userId = userId))
              Ok(Json.obj("status" -> "OK"))}
          )
      )
    }
  }


  def lookupDefAsync(word:String) = Action {

/*    val a = Enumerator(Future {
      LongmanContemporaryScraper.scrape(word)}
    )

    val b = Enumerator(Future {
      OxfordLearnerScraper.scrape(word)}
    )

    val c = Enumerator(Future {
      CambridgeLearnerScraper.scrape(word)}
    )

    Ok.chunked(a >- b >- c &> Comet(callback = "console.log"))*/
    Ok
  }
}

object JsonValidators {
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  implicit val ratingReads : Reads[(Long, Int)] = (
    (__ \ "id").read[Long] and
    (__ \ "value").read[Int](min(1) keepAnd max(5))
  ) tupled

  implicit val entryReads : Reads[(String, String)] = (
    (__ \ "title").read[String] and
      (__ \ "content").read[String]
    ) tupled
}
