package security

import controllers.routes
import models.User
import play.api.mvc._

/**
 * Created by znw on 7/13/14.
 */
trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.UserController.signIn())

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.getUserByName(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  /**
   * assuming password doesn't contain :
   * @param auth
   * @return
   */
  def decodeBasicAuth(auth: String) = {
    val baStr = auth.replaceFirst("Basic ", "")
    val Array(user, pass) = new String(new sun.misc.BASE64Decoder().decodeBuffer(baStr), "UTF-8").split(":")
    (user, pass)
  }



}
