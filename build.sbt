ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

val pekkoVersion = "1.1.3"
val logbackVersion = "1.5.16"

lazy val root = (project in file("."))
  .settings(
    name := "SmartHomeAlarmSystem",
    libraryDependencies ++= Seq(
      // Modulo principale di Pekko Actor Typed
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,

      // Logger compatibile per vedere i log di Pekko nella console
      "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion
    )
  )