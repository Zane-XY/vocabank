package controllers

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import models._

import org.joda.time.DateTime


import play.filters.csrf.CSRF.Token._

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

  //views.html.add_entry(formWithErrors)
  def save = Action {
    implicit request =>
      entryF.bindFromRequest.fold(
        formWithErrors => Ok(views.html.entry(formWithErrors)),
        entry => {
          Entry.save(entry)
          Redirect(routes.EntryController.entries).flashing("success" -> "Entry saved")
        })
  }
}
