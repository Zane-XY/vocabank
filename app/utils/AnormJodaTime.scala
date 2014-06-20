package anorm.jodatime

import org.joda.time._
import org.joda.time.format._
import anorm._

object Extension{

  val dateTimeParser = ISODateTimeFormat.dateTimeParser()

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case d: java.sql.Date => Right(new DateTime(d.getTime))
      case str: java.lang.String => Right(dateTimeParser.parseDateTime(str))
      case _ => Left(TypeDoesNotMatch("Can't convert to date and time " + value + ":" + value.asInstanceOf[AnyRef].getClass) )
    }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, value: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(value.getMillis()) )
    }
  }
}
