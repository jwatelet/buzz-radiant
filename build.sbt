name := "BuzzRadiant"

version := "0.1"

scalaVersion := "2.12.4"

enablePlugins(DockerPlugin)


Compile/mainClass := Some("be.jwa.Boot")


libraryDependencies ++= {
  val akkaV = "10.1.5"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaV,
    "com.twitter" % "hbc-core" % "2.2.0",
    "com.twitter" % "hbc-twitter4j" % "2.2.0",
    "org.slf4j" % "slf4j-simple" % "1.7.21"
  )
}

mainClass := Some("Boot")

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}