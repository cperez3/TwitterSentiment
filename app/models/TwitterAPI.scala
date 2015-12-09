package models

import javax.inject.Inject
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import play.api.libs.ws._
import scala.concurrent.{Await, Future}
import play.api.libs.json._

import scala.concurrent.duration.Duration

// JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import  scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by chris_000 on 12/5/2015.
 */
class TwitterAPI @Inject() (ws: WSClient) {
  val TWITTER_API_KEY = "euoX2XoXH9Q5WY4AzVwitVdZf"
  val TWITTER_API_SECRET = "1bxT0CSIcWdNOg9pUhf7wPtsyCzLK99kd7lXRHEdZyK2HgaSs1"
  val TWITTER_API_CREDENTIALS = TWITTER_API_KEY + ":" + TWITTER_API_SECRET
  val encodedCredentials:String = BaseEncoding.base64().encode(TWITTER_API_CREDENTIALS.getBytes(Charsets.UTF_8))

  var endpoint: String = "https://api.twitter.com/1.1/search/tweets.json"
  var bearerToken = ""

  def getTweets(q: String, result_type: String, lat: Int, long: Int, radius: Int): String = {
    val url = endpoint + "?q=" + q + "&result_type=" + result_type + "&geocode=" + lat + "," + long + "," + radius + "mi"
    bearerToken = Await.result(authorize(), Duration.Inf)
    val futureResult: Future[String] = ws.url(url).withHeaders("Authorization" -> ("Bearer " + bearerToken)).get().map {
      response => response.body.toString
    }
    Await.result(futureResult, Duration.Inf)
  }

  def getTweets(q: String, result_type: String): String = {
    val url = endpoint + "?q=" + q + "&result_type=" + result_type
    bearerToken = Await.result(authorize(), Duration.Inf)
    val futureResult: Future[String] = ws.url(url).withHeaders("Authorization" -> ("Bearer " + bearerToken)).get().map {
      response => response.body.toString
    }
    Await.result(futureResult, Duration.Inf)
  }

  def getTweetsByHashTag(hashTag: String): String = {
    getTweets("%23" + hashTag, "recent")
  }

  def authorize(): Future[String] = {
    val url = "https://api.twitter.com/oauth2/token"
    val futureResult: Future[String] = ws.url(url)
      .withHeaders("Authorization" -> ("Basic " + encodedCredentials)).post(Map("grant_type"-> Seq("client_credentials"))).map(
          response => (response.json \ "access_token").as[String]
//        response => "Response: " + response + "\nBody: " + response.body.toString
      )
    futureResult
  }
}