enablePlugins(JavaAppPackaging)

name := """eCabRest"""
organization := "com.suhailkandanur"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.3"
  val scalaTestV  = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "ch.qos.logback" % "logback-core" % "1.1.7",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.yaml" % "snakeyaml" % "1.17",
    "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
    "org.apache.tika" % "tika-core" % "1.13",
    "net.liftweb" % "lift-json_2.10" % "2.6.3",
    "com.google.code.gson" % "gson" % "2.7",
    "org.springframework.shell" % "spring-shell" % "1.2.0.RELEASE"



  )
}

Revolver.settings
