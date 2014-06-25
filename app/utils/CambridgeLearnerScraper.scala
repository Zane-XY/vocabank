package utils

import org.jsoup.Jsoup

/**
 * Created by zane.wang on 6/23/2014.
 */
object CambridgeLearnerScraper {
  val dict = "http://dictionary.cambridge.org/dictionary/learner-english/"

  def scrape(word : String) : String = {
   val res = Jsoup.connect(dict + word).timeout(60*1000).ignoreHttpErrors(true).execute
   res.statusCode match {
      case 200 => val doc = res.parse().select("div.di")
                  doc.select("div.share-this-entry").remove
                  doc.select("div.di-title").remove
                  doc.toString
        case _ => ""
    }
  }

}
