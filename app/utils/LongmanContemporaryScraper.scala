package utils

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zane.wang on 6/23/2014.
 */
object LongmanContemporaryScraper {
  val dict = "http://www.ldoceonline.com"
  val search = "/search/?q="
  val entry = ""

  def scrapeEntry(link : String) =  {
    val doc = Jsoup.connect(link).get.select("div.Entry")
      doc.select("script, iframe").remove
      doc.toString
  }

  def scrape(word : String) : String = {
   val res = Jsoup.connect(dict + search + word).ignoreHttpErrors(true).execute
   res.statusCode match {
      case 200 => val doc = res.parse().select("a:has(span:matchesOwn(^" + word + "$))")
                  val urls = doc.iterator.toList.map(dict + _.attr("href"))
                  //Future.traverse(urls)(scrapeEntry).value.get.get.mkString("\n")
                  urls.par.map(scrapeEntry).mkString("\n")
      case _ => ""
    }
  }

}
