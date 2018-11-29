name := "BuzzRadiant"

version := "0.1"

scalaVersion := "2.12.4"

enablePlugins(JavaAppPackaging)
mappings in (Compile, packageDoc) := Seq()
Compile/mainClass := Some("be.jwa.Boot")

libraryDependencies ++= {
  val akkaV = "10.1.5"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaV,
    "com.typesafe.akka" %% "akka-actor" % "2.5.18",
    "com.typesafe.akka" %% "akka-stream" % "2.5.18",
    "com.twitter" % "hbc-core" % "2.2.0",
    "com.twitter" % "hbc-twitter4j" % "2.2.0",
    "org.slf4j" % "slf4j-simple" % "1.7.21"
  )
}
