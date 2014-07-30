package utils

import org.jsoup.Jsoup
import play.api.Logger

/**
 * Created by zane.wang on 6/23/2014.
 */
object CambridgeLearnerScraper {
  val dict = "http://dictionary.cambridge.org/dictionary/learner-english/"

  def scrape(word : String) : String = {
   val t = word.replaceAll("[\\s|']+", "-").toLowerCase

   val res = Jsoup.connect(dict + t).timeout(60*1000).ignoreHttpErrors(true).execute
   res.statusCode match {
      case 200 => val doc = res.parse().select("div.di").addClass("entrybox learner-english")
                  doc.select("div.share-this-entry").remove
                  doc.select("div.di-title").remove
                  doc.toString
      case _ => ""
    }
  }

}
