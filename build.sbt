name := "BuzzRadiant"

version := "0.1"

scalaVersion := "2.12.4"

enablePlugins(JavaAppPackaging)
mappings in(Compile, packageDoc) := Seq()
Compile / mainClass := Some("be.jwa.Boot")

libraryDependencies ++= {
  val akkaHttpVersion = "10.1.5"
  val akkaVersion = "2.5.18"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.0-SNAP9" % Test,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.twitter" % "hbc-core" % "2.2.0",
    "com.twitter" % "hbc-twitter4j" % "2.2.0",
    "org.slf4j" % "slf4j-simple" % "1.7.21"
  )
}

libraryDependencies ~= {
  _.map(_.exclude("org.slf4j", "slf4j-simple"))
}