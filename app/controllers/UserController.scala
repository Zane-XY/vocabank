package controllers

import org.joda.time.DateTime
import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import models.User
import utils.ReCaptchaUtils
import play.filters.csrf._
import play.filters.csrf.CSRF.Token._
object UserController extends Controller {

val captchaF = Form[(String, String)](
 tuple(
    "recaptcha_challenge_field" -> nonEmptyText,
    "recaptcha_response_field" -> nonEmptyText
  )
 )

  val userF = Form[User](
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> email)(
        (name, password, email) => User(name, email, password, new DateTime))
        ((u:User) => Some(u.name, "",  u.email)))

  def signUp = Action { implicit req =>
    Ok(views.html.signUp(userF))
  }


  def save = Action { implicit req =>
    val cF = captchaF.bindFromRequest
    val uF = userF.bindFromRequest

    cF.fold(
      err => ( BadRequest("Captcha Param Error")),
      { case (q, a) => {
          if (ReCaptchaUtils.check(req.remoteAddress, q, a)) {
            Logger.debug(uF.value.toString)
            Ok
          } else {
            BadRequest("Captcha Validation Error")
          }
        }
      }
    )
  }

}
