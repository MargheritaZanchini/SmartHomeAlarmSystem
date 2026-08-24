
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

val PekkoVersion = "1.7.0"
val logbackVersion = "1.5.16"

lazy val root = (project in file("."))
  .settings(
    name := "SmartHomeAlarmSystem",
    run / fork := true,
    assembly / assemblyOutputPath := baseDirectory.value / "target" / "app.jar",
    libraryDependencies ++= Seq(
      // Modulo principale di Pekko Actor Typed
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-sharding-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson" % PekkoVersion,

      // Logger compatibile per vedere i log di Pekko nella console
      "org.apache.pekko" %% "pekko-slf4j" % PekkoVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
      case PathList("module-info.class")                              => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )