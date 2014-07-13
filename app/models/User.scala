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
                 registerDate: DateTime
                 )

object User {

  private val userRowParser: RowParser[User] = {
    get[Option[Long]]("id") ~
      get[String]("name") ~
      get[String]("email") ~
      get[String]("password") ~
      get[DateTime]("registerDate") map {
      case id ~ name ~ email ~ password ~ registerDate =>
        User(id, name, email, password, registerDate)
    }
  }

  /**
   * return auth status and messages
   * @param pStr
   * @param email
   * @return
   */
  def auth(email: String, pStr: String):(Option[User], String) = {
    DB.withConnection { implicit connection =>
        SQL("SELECT * FROM USERS WHERE EMAIL = {email} LIMIT 1").on('email -> email).as(userRowParser.singleOpt)
    }.fold((None:Option[User]) -> "error.user.not.found")(
      u => if (BCrypt.checkpw(pStr, u.password))
             Some(u) -> "info.user.signedIn"
           else
             None -> "error.user.password.incorrect"
    )
  }

  def getUserByName(username:String):Option[User] = {
    DB.withConnection{ implicit conn =>
      SQL(
        """
         SELECT * FROM USERS WHERE USERNAME = {username} LIMIT 1
        """.stripMargin).on('username -> username).as(userRowParser.singleOpt)
    }
  }

  def save(r: User):(Option[Long], String) = {
    DB.withConnection { implicit connection =>
      val u = SQL(
        """
          SELECT COUNT(*) FROM USERS WHERE EMAIL = {email}
        """).on('email -> r.email).as(scalar[Long].single)
      if (u == 0L) {
        (SQL( """
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
          ).executeInsert(scalar[Long].singleOpt), "info.user.register.success")
     } else (None, "error.user.exists")
    }

  }

}
