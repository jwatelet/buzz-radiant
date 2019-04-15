package be.jwa.controllers


import twitter4j.{GeoLocation, Place, Status, User}


case class Tweet(id: Long, createdAt: Long, tweetText: String, hashTags: Seq[String], user: TwitterUser,
                 place: Option[TwitterPlace], geolocation: Option[TwitterGeolocation], isRetweet: Boolean)

case class TwitterPlace(country: Option[String], countryCode: Option[String], id: Option[String], placeType: Option[String],
                        url: Option[String], streetAddress: Option[String])

case class TwitterGeolocation(latitude: Double, longitude: Double)

case class TwitterUser(id: Long, name: String, email: Option[String], lang: String, followersCount: Int, friendsCount: Int,
                       description: Option[String], createdAt: Long)

trait TwitterExtractor {

  def extractTweet(status: Status): Tweet = {

    val id = status.getId
    val createdAt = status.getCreatedAt.getTime
    val tweetText = status.getText
    val hashTags = status.getHashtagEntities.toSeq.map(ht => ht.getText.toLowerCase)
    val isRetweet = status.isRetweet

    val place: Option[TwitterPlace] = Option(status.getPlace).map(extractPlace)
    val geoLocation = Option(status.getGeoLocation).map(extractGeolocation)
    val user = extractUser(status.getUser)

    Tweet(id, createdAt, tweetText, hashTags, user, place, geoLocation, isRetweet)
  }

  private def extractUser(user: User) = {

    val id: Long = user.getId
    val name: String = user.getName
    val lang: String = user.getLang
    val followersCount: Int = user.getFollowersCount
    val friendsCount: Int = user.getFriendsCount
    val description: String = user.getDescription
    val createdAt = user.getCreatedAt.getTime
    val email = Option(user.getEmail)
    TwitterUser(id, name, email, lang, followersCount, friendsCount, Option(description), createdAt)
  }

  private def extractPlace(place: Place) = {

    val country = Option(place.getCountry)
    val countryCode = Option(place.getCountryCode)
    val id = Option(place.getId)
    val placeType = Option(place.getPlaceType)
    val url = Option(place.getURL)
    val streetAddress = Option(place.getStreetAddress)

    TwitterPlace(country, countryCode, id, placeType, url, streetAddress)
  }

  private def extractGeolocation(geoLocation: GeoLocation) = {
    val latitude = geoLocation.getLatitude
    val longitude = geoLocation.getLongitude
    TwitterGeolocation(latitude, longitude)
  }
}