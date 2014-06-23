package utils

import org.jsoup.Jsoup

/**
 * Created by zane.wang on 6/23/2014.
 */
object CambridgeLearnerScraper {
  val dict = "http://dictionary.cambridge.org/dictionary/learner-english/"

  def scrape(word : String) : String = {
   val res = Jsoup.connect(dict + word).ignoreHttpErrors(true).execute
   res.statusCode match {
      case 200 => res.parse().select("div.di").toString
      case _ => ""
    }
  }

}
