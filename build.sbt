offline := true

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.twitter.finatra" %% "finatra-http" % "2.1.5",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.mchange" % "c3p0" % "0.9.5",
  "com.h2database" % "h2" % "1.4.191"
)

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case other => MergeStrategy.defaultMergeStrategy(other)
}
