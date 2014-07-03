package models

import anorm.RowParser
import org.joda.time.DateTime
import anorm.jodatime.Extension._
import org.mindrot.jbcrypt.BCrypt

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(
                 id: Option[Long],
                 name: String,
                 email: String,
                 password: String,
                 registerDate: DateTime = new DateTime
                 )

object User {

  private val rowParser: RowParser[User] = {
    get[Option[Long]]("id") ~
      get[String]("name") ~
      get[String]("email") ~
      get[String]("password") ~
      get[DateTime]("registerDate") map {
      case id ~ name ~ email ~ password ~ registerDate =>
        User(id, name, email, password, registerDate)
    }
  }

  def auth(pStr: String, email: String) = {
    val pHash = DB.withConnection { implicit connection =>
        SQL("SELECT PASSWORD FROM USERS WHERE EMAIL = {email} LIMIT 1").on('email -> email).as(scalar[String].single)
    }
    BCrypt.checkpw(pStr, pHash)
  }

  def save(r: User) {
    DB.withConnection { implicit connection =>
      SQL( """
              INSERT INTO USERS (
                  NAME,
                  EMAIL,
                  PASSWORD,
                  REGISTERDATE
              ) VALUES ({name}, {email}, {password},{registerDate})
           """).on(
          'name -> r.name,
          'email -> r.email,
          'password -> BCrypt.hashpw(r.password, BCrypt.gensalt()),
          'registerDate -> r.registerDate
        ).executeInsert()
    }
  }
}
