package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit req =>
    Ok(views.html.search("search your word!"))
  }

}
