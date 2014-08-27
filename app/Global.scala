import models.Entry
import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.filters.csrf._
import play.api.Play.current

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {
  // ... onStart, onStop etc
  override def onStart(app: Application) {
    Logger.info("loading tags into cache")
    Cache.set("tags", Entry.loadTags)
  }
}
