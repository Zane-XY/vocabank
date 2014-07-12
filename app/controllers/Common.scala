package controllers

import play.api.i18n.Messages
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Controller, Request}

import scala.util.Try

/**
 * Created by znw on 7/12/14.
 */
object Common extends  Controller {

  def fromSession[A,B](key:String, f : String => B)(implicit req:Request[A]):Option[B] =
    req.session.get(key).flatMap(v => Try(f(v)).toOption)

  def userIdFromSession[A](implicit req:Request[A]):Option[Long] = fromSession("userId", _.toLong)

  def userEmailFromSession[A](implicit req:Request[A]):Option[String] = fromSession("email", _.toString)

  val NotSignedIn = BadRequest(Json.obj("status" -> "KO" , "error" -> Messages("error.user.not.signedIn")))

  def BadRequestJSON (e: JsError) = BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e)))
}
