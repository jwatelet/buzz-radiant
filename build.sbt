name := "BuzzRadiant"

version := "0.1"

scalaVersion := "2.12.4"

enablePlugins(JavaAppPackaging)
mappings in(Compile, packageDoc) := Seq()
Compile / mainClass := Some("be.jwa.Boot")

libraryDependencies ++= {
  val akkaHttpVersion = "10.1.8"
  val akkaVersion = "2.5.22"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.0-SNAP9" % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "joda-time" % "joda-time" % "2.10.1",
    "org.twitter4j" % "twitter4j-stream" % "4.0.7"
  )
}