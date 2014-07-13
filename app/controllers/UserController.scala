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
import play.api.Play.current

object UserController extends Controller {

  def captchaF(implicit req: Request[AnyContent]) = Form[(String, String)](
    tuple(
      "recaptcha_challenge_field" -> nonEmptyText,
      "recaptcha_response_field" -> nonEmptyText
    ) verifying(fields => fields match {
      case (q, a) => if (Play.isDev) true else ReCaptchaUtils.check(req.remoteAddress, q, a)
    })
  )

  val signInF = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 6, maxLength = 18)
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

  def signUpSubmit = CSRFCheck {
    Action { implicit req =>
      val form = userF.bindFromRequest
      form.fold(
        _ => BadRequest(views.html.signUp(form)),
        {case user: User =>
            captchaF.bindFromRequest.fold(
              _ => BadRequest(views.html.signUp(form.withError("recaptcha", "error.recaptcha.invalid"))),
              _ =>  User.save(user) match {
                    case (None, m) => BadRequest(views.html.signUp(form.withError("error", m)))
                    case (_, m) => Redirect(routes.UserController.signIn).flashing("info" -> m)
              }
            )
        }
      )
    }
  }

  def signIn = Action { implicit req =>
    Ok(views.html.signIn(signInF))
  }

  def signOut = Action { implicit req =>
    Redirect(routes.Application.index()).withNewSession
  }

  def signInSubmit = CSRFCheck {
    Action { implicit req =>
      val form = signInF.bindFromRequest
      form.fold(
        _ => BadRequest(views.html.signIn(form)),
        {case t @ (e, p) =>
          captchaF.bindFromRequest.fold(
            _ => BadRequest(views.html.signIn(form.withError("recaptcha", "error.recaptcha.invalid"))),
            _ => User.auth(e, p) match {
                   case (Some(u @ user) , msg) =>
                     Redirect(routes.EntryController.entries)
                       .flashing("msg" -> msg)
                       .withSession("signedIn" -> u.email, "userId" -> u.id.fold("")(_.toString))
                   case (_, msg) => BadRequest(views.html.signIn(form.withError("password", msg)))
            }
          )
        }
      )
    }
  }

}
