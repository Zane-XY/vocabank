resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/"

//play sbt-plugin snapshot
resolvers += Resolver.url("Typesafe Simple Snapshots", url("https://repo.typesafe.com/typesafe/simple/snapshots/"))(Resolver.ivyStylePatterns)

//play snapshot
resolvers +=  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// The Play plugin
addSbtPlugin("com.typesafe.play" %% "sbt-plugin" % "2.4-SNAPSHOT")

// web plugins

// addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")
//
// addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")
//
// addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")
//
// addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")
//
// addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")
//
// addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")
