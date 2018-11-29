package be.jwa

import org.slf4j.LoggerFactory

class Config(config: com.typesafe.config.Config) {

  val logger = LoggerFactory.getLogger(classOf[Config])

  val twitterConsumerKey: String = Option(System.getenv("TWITTER_CONSUMER_KEY")).getOrElse(config.getString("twitter.consumerKey"))
  logger.info("twitterConsumerKey" + twitterConsumerKey)
  val twitterConsumerSecret: String = Option(System.getenv("TWITTER_CONSUMER_SECRET")).getOrElse(config.getString("twitter.consumerSecret"))
  logger.info("twitterConsumerSecret" + twitterConsumerSecret)
  val twitterToken: String = Option(System.getenv("TWITTER_TOKEN")).getOrElse(config.getString("twitter.token"))
  logger.info("twitterToken" + twitterToken)
  val twitterSecret: String = Option(System.getenv("TWITTER_SECRET")).getOrElse(config.getString("twitter.secret"))
  logger.info("twitterSecret" + twitterSecret)
  val twitterConfig: (String, String, String, String) = (twitterConsumerKey, twitterConsumerSecret, twitterToken, twitterSecret)
}

