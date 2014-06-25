package controllers

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Comet
import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import models._

import org.joda.time.DateTime

import play.filters.csrf._
import play.filters.csrf.CSRF.Token._
import utils.{LongmanContemporaryScraper, CambridgeLearnerScraper}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

//import play.api.libs.json._
//import play.api.libs.json.Reads._
//import play.api.libs.functional.syntax._

object EntryController extends Controller {

  def formToEntry(id: Option[Long],
                  title: String,
                  source: Option[List[String]],
                  content: String,
                  tags: Option[List[String]],
                  rating: Int): Entry = {
    val now = new DateTime
    Entry(None, title, source, content, now, now, tags, rating)
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
  def save = Action { implicit request =>
      entryF.bindFromRequest.fold(
        formWithErrors => Ok(views.html.entry(formWithErrors)),
        entry => {
          Entry.save(entry)
          Redirect(routes.EntryController.entries).flashing("success" -> "Entry saved")
        })
  }


  def lookupDefAsync(word: String) = Action { implicit  req =>
    //Ok(CambridgeLearnerScraper.scrape(word))
    val a = Enumerator( Await.result(Promise.timeout("A", 5 seconds), 1 minute))
    val b = Enumerator( Await.result(Promise.timeout("B", 3 seconds), 1 minute))
    val c = Enumerator( Await.result(Promise.timeout("C", 1 second), 1 minute))

    val d = a >- b >- c

    Ok.chunked(d &> Comet(callback = "console.log"))
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
}
