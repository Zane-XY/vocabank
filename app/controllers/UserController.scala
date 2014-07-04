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

  def captchaF(implicit req: Request[AnyContent]) = Form[(String, String)](
    tuple(
      "recaptcha_challenge_field" -> nonEmptyText,
      "recaptcha_response_field" -> nonEmptyText
    ) verifying("validation error", fields => fields match {
      case (q, a) => ReCaptchaUtils.check(req.remoteAddress, q, a)
    })
  )

  val signInF = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText
    )
  )

  val userF = Form[User](
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> email)(
        (name, password, email) => User(None, name, email, password, new DateTime))
        ((u: User) => Some(u.name, "", u.email))
  )

  def signUp = Action { implicit req =>
    Ok(views.html.signUp(userF))
  }

  def signIn = Action { implicit req =>
    Ok(views.html.signIn(signInF))
  }

  def signInAuth = CSRFCheck {
    Action { implicit req =>
      val signInForm = signInF.bindFromRequest
      signInForm.fold(
        _ => (Ok(views.html.signIn(signInForm))),
        { case t @ (e, p) =>
            captchaF.bindFromRequest.fold(
              _ => Ok(views.html.signIn(signInForm)),
              _ => {
                val (signedIn, msg) = User.auth(e, p)
                if (signedIn)
                  Redirect(routes.EntryController.entries).flashing("msg" -> msg)
                else
                  Redirect(routes.UserController.signIn).flashing(Flash(signInForm.data) + ("msg" -> msg))
              }
            )
        })
    }
  }

  def save = CSRFCheck {
    Action { implicit req =>
      val uf = userF.bindFromRequest
      uf.fold(
        _ => (Ok(views.html.signUp(uf))),
        { case user: User =>
              captchaF.bindFromRequest.fold(
                _ => Ok(views.html.signUp(userF)),
                _ => {
                  User.save(user)
                  Redirect(routes.EntryController.entries).flashing("success" -> "Entry saved")
                })
        })
    }
  }

}
