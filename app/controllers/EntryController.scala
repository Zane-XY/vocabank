package controllers

import controllers.Common._
import models._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF.Token._
import play.filters.csrf._

object EntryController extends Controller {

  val entryF = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "source" -> optional(list(text)),
      "content" -> text,
      "tags" -> optional(list(text)),
      "rating" -> number(min = 1, max = 5)
    )(Entry.assemble)(Entry.disassemble))

  def entries = Action { implicit req =>
    userIdFromSession.fold(
      BadRequest(views.html.errors("error.user.not.signedIn")))(userId =>
      Ok(views.html.entries(Entry.listAll(userId)))
    )
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
          case (id, value) => Ok(Json.obj( "status" -> "OK", "updated" -> Entry.setRating(id, value, userId)))
        }.recoverTotal(BadRequestJSON))
   }
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
  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val ratingReads : Reads[(Long, Int)] = (
    (__ \ "id").read[Long] and
    (__ \ "value").read[Int](min(1) keepAnd max(5))
  ) tupled

  implicit  val entryReads:Reads[Entry] = (
      (__ \ "id").read[Option[Long]] and
      (__ \ "title").read[String] and
      (__ \ "source").read[Option[List[String]]] and
      (__ \ "content").read[String] and
      (__ \ "tags").read[Option[List[String]]] and
      (__ \ "rating").read[Int]
    )(Entry.assemble _)


  implicit val entryIdReads : Reads[Long] = (__ \ "id").read[Long]

}
