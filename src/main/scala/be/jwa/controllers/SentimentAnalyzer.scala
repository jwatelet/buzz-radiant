package be.jwa.controllers

import java.util.Properties

import be.jwa.controllers.Sentiment._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.ejml.simple.SimpleMatrix

import scala.collection.JavaConverters._

trait SentimentAnalyzer {

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline = new StanfordCoreNLP(props)

  def sentiment(txt: String): Sentiment = {
    var sm = new SimpleMatrix(5, 1)

    val annotation = pipeline.process(txt)
    val sentences: java.util.List[CoreMap] = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentences.asScala.foreach { sentence =>
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sentenceLength = sentence.toString.length
      val factor = SimpleMatrix.diag(sentenceLength)
      sm = sm.plus(RNNCoreAnnotations.getPredictions(tree).mult(factor))
    }
    val den = 1 / txt.length.toFloat

    sm = sm.mult(SimpleMatrix.diag(den))

    val sents = Map(VeryNegative -> sm.get(0), Negative -> sm.get(1), Neutral -> sm.get(2), Positive -> sm.get(3), VeryPositive -> sm.get(4))
    sents.maxBy(_._2)._1
  }

}