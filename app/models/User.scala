package models

import org.joda.time.DateTime

case class User(
    name: String,
    email: String,
    password: String ,
    registerDate:DateTime = new DateTime
)
