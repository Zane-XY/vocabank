package controllers

import anorm._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import models._

object EntryController extends Controller {

  val entryF = Form( mapping(
      "id" -> optional(Long),
      "title" -> nonEmptyText,
      "source" -> list(text),
      "added" -> jodaDate,
      "updated" -> jodaDate,
      "tags" -> list(text),
      "content" -> text,
      "rating" -> number(min=1, max=5))(Entry.apply)(Entry.unapply))

  def entries = Action {
      Ok(views.html.entries(""))
  }

  def saveEntry = Action { implicit request =>
    entryF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.entry.add(formWithErrors)),
      entry => {
        Entry.save(entry)
        Redirect(routes.Entry.list).flashing("success" -> "Entry saved")
      })
  }
}
