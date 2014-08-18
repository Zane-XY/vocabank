package utils

import org.jsoup.Jsoup

/**
 * Created by zane.wang on 8/14/2014.
 */
object SoundScraper {
  val src = "http://www.oxforddictionaries.com/us/definition/american_english/"

  def scrape(word : String) : Option[String] = {
    val res = Jsoup.connect(src + word).timeout(60 * 1000).ignoreHttpErrors(true).execute
    res.statusCode match {
      case 200 => Some(res.parse().select("div.sound").attr("data-src-mp3"))
      case _ => None
    }
  }
}
