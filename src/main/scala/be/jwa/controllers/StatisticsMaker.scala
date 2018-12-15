package be.jwa.controllers

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer

case class TwitterStatistics(timeStatistics: Map[String, TimeStatistic])

case class TimeStatistic(timeInMillis: Long, numberOfTweets: Int)


trait StatisticsMaker {

  def makeStatistics(tweetBuffer: ListBuffer[Tweet]): TwitterStatistics = {

    val timeStat = makeTimeStatistic(tweetBuffer)

    TwitterStatistics(timeStat)
  }

  private def makeTimeStatistic(tweetBuffer: ListBuffer[Tweet]) = tweetBuffer.groupBy(t => roundTime(t.createdAt, 10))
    .map { case (timeInMillis, buffer) =>

      new Date(timeInMillis).toString -> TimeStatistic(timeInMillis, buffer.size)
    }

  private def roundTime(timeInMillis: Long, windowMinutes: Int): Long = {
    val dt = new DateTime(timeInMillis, DateTimeZone.UTC)
    dt.withMinuteOfHour((dt.getMinuteOfHour / windowMinutes) * windowMinutes)
      .minuteOfDay
      .roundFloorCopy
      .toDate
      .getTime
  }
}