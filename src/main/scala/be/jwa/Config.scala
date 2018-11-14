package be.jwa

case class Config(config: com.typesafe.config.Config) {
  val twitterConsumerKey = config.getString("twitter.consumerKey")
  val twitterConsumerSecret = config.getString("twitter.consumerSecret")
  val twitterToken = config.getString("twitter.token")
  val twitterSecret = config.getString("twitter.secret")
  val twitterConfig = (twitterConsumerKey, twitterConsumerSecret, twitterToken, twitterSecret)
}

