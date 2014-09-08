import models.Entry
import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.filters.csrf._
import play.api.Play.current

import scala.concurrent.duration._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {
  // ... onStart, onStop etc
  override def onStart(app: Application) {
    Logger.debug("loading tags into cache" + Entry.loadTags)
    Cache.set("tags", Entry.loadTags, expiration = 365.days)
  }
}
