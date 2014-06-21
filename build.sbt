name := """vocabank"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.178",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  filters, jdbc, anorm, cache, ws
)

transitiveClassifiers := Seq(Artifact.SourceClassifier)