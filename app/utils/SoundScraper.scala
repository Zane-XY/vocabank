package utils

import org.jsoup.Jsoup
import scalaz._
import std.option._
import syntax.plus._

/**
 * Created by zane.wang on 8/14/2014.
 */

object SoundScraper {
  val oxford = "http://www.oxforddictionaries.com/us/definition/american_english/"
  val cambridge = "http://dictionary.cambridge.org/us/dictionary/british/"
  
  def scrape(word : String) : Option[String] =  scrapeOxford(word) <+> scrapeCambridge(word)

  def scrapeOxford(word : String) : Option[String] = {
    val res = Jsoup.connect(oxford + word).timeout(60 * 1000).ignoreHttpErrors(true).execute
    res.statusCode match {
      case 200 => Some(res.parse().select("div.sound").attr("data-src-mp3"))
      case _ => None
    }
  }

  def scrapeCambridge(word : String) : Option[String] = {
    val res = Jsoup.connect(cambridge + word).timeout(60 * 1000).ignoreHttpErrors(true).execute
    res.statusCode match {
      case 200 => Some(res.parse().select("a.pron-us").attr("data-src-mp3"))
      case _ => None
    }
  }

}

