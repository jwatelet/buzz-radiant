package be.jwa.controllers


import twitter4j.{Status, User}

case class Tweet(id: Long, user: TwitterUser, tweetText: String, placeName: Option[String], hashTags: Seq[String])

case class TwitterUser(id: Long, name: String, lang: String, followersCount: Int, friendsCount: Int, description: String)

trait TweetExtractor {

  def extractTweet(status: Status): Tweet = {

    val id = status.getId
    val tweetText: String = status.getText
    val placeName: Option[String] = if (status.getPlace != null) Some(status.getPlace.getName) else None
    val hashTags: Seq[String] = status.getHashtagEntities.toSeq.map(ht => ht.getText.toLowerCase)

    val user = extractUser(status.getUser)

    Tweet(id, user, tweetText, placeName, hashTags)
  }

  private def extractUser(user: User): TwitterUser = {

    val id: Long = user.getId
    val name: String = user.getName
    val lang: String = user.getLang
    val followersCount: Int = user.getFollowersCount
    val friendsCount: Int = user.getFriendsCount
    val description: String = user.getDescription
    TwitterUser(id, name, lang, followersCount, friendsCount, description)
  }

}
