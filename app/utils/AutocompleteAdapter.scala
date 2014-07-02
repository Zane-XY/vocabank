package utils

import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by zane.wang on 6/30/2014.
 */
object AutocompleteAdapter {
   val oxfordService = "http://www.oxforddictionaries.com/autocomplete/american_english/?multi=1"
   val cambridgeService = "http://dictionary.cambridge.org/autocomplete/learner-english/"

  def query(t: String) = {
    WS.url(cambridgeService).withQueryString("q" -> t).get.map(r => Json.toJson(r.json \\ "searchtext"))
  }

}
