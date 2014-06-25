package utils

import org.jsoup.Jsoup

/**
 * Created by zane.wang on 6/23/2014.
 */
object OxfordLearnerScraper {
  val dict = "http://www.oxfordlearnersdictionaries.com/us/definition/american_english/"

  def scrape(word : String) : String = {
   val res = Jsoup.connect(dict + word).timeout(60*1000).ignoreHttpErrors(true).execute
   res.statusCode match {
      case 200 => val doc = res.parse().select("#ox-container")
                  val entryContent = doc.select("div#entryContent")
                  val relatedEntries = doc.select("relatedentries")
                  entryContent.toString + relatedEntries.toString
      case _ => ""
    }
  }

}
