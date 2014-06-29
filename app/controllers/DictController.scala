package controllers

import play.api.mvc._
import utils.{CambridgeLearnerScraper, OxfordLearnerScraper, LongmanContemporaryScraper}

/**
 * Created by zane.wang on 6/25/2014.
 */
object DictController extends Controller {

  def lookupDef(dictid: String, word: String) = Action {
    dictid match {
      case "longman" => Ok(LongmanContemporaryScraper.scrape(word))
      case "cambridge" => Ok(CambridgeLearnerScraper.scrape(word))
      case "oxford" => Ok(OxfordLearnerScraper.scrape(word))
      case _ => Ok
    }

  }

  def lookupCambridge(word: String) = Action {
    Ok(CambridgeLearnerScraper.scrape(word))
  }

  def lookupOxford(word: String) = Action {
    Ok(OxfordLearnerScraper.scrape(word))
  }

  def lookupLongman(word: String) = Action {
    Ok(LongmanContemporaryScraper.scrape(word))
  }
}
