name := """vocabank"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "sbt Snapshots" at "http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"

libraryDependencies ++= Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7",
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.h2database" % "h2" % "1.4.178",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  filters, jdbc, anorm, cache, ws
)

transitiveClassifiers := Seq(Artifact.SourceClassifier)
