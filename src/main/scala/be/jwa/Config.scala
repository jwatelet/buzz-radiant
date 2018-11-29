package be.jwa

import org.slf4j.LoggerFactory

class Config(config: com.typesafe.config.Config) {

  private val logger = LoggerFactory.getLogger(classOf[Config])

  val twitterConsumerKey: String = Option(System.getenv("TWITTER_CONSUMER_KEY")).getOrElse(config.getString("twitter.consumerKey"))
  val twitterConsumerSecret: String = Option(System.getenv("TWITTER_CONSUMER_SECRET")).getOrElse(config.getString("twitter.consumerSecret"))
  val twitterToken: String = Option(System.getenv("TWITTER_TOKEN")).getOrElse(config.getString("twitter.token"))
  val twitterSecret: String = Option(System.getenv("TWITTER_SECRET")).getOrElse(config.getString("twitter.secret"))
  val twitterConfig: (String, String, String, String) = (twitterConsumerKey, twitterConsumerSecret, twitterToken, twitterSecret)
  logger.info(s"twitterSecret $twitterConfig")
}

