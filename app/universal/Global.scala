package universal

import models.Entry
import play.api._
import play.api.mvc._
import play.filters.csrf._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  lazy val tagsCache = Entry.loadTags
  // ... onStart, onStop etc
  override def onStart(app: Application) {
  }

}
