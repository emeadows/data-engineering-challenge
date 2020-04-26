import sbt._

object Dependencies {

  private val kafkaVersion:           String = "2.5.0"
  private val pureConfigVersion:      String = "0.12.3"
  private val scalaLoggingVersion:    String = "3.9.2"
  private val logbackVersion:         String = "1.2.3"
  private val scalaTestVersion:       String = "3.1.1"
  private val circeVersion:           String = "0.13.0"
  private val kafkaSerdeCirceVersion: String = "0.4.0"

  val all: Seq[ModuleID] = ProductionDependencies.values ++ TestDependencies.values

  private[this] object ProductionDependencies {

    val values: Seq[ModuleID] = logging ++ pureConfig ++ kafkaStreams ++ json ++ serde

    private lazy val kafkaStreams: Seq[ModuleID] = Seq("org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion)

    private lazy val logging: Seq[ModuleID] =
      Seq("com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion, "ch.qos.logback" % "logback-classic" % logbackVersion)

    private lazy val pureConfig: Seq[ModuleID] = Seq("com.github.pureconfig" %% "pureconfig" % pureConfigVersion)

    private lazy val json: Seq[ModuleID] =
      Seq("io.circe" %% "circe-parser" % circeVersion, "io.circe" %% "circe-generic" % circeVersion)

    private lazy val serde: Seq[ModuleID] =
      Seq("io.github.azhur" %% "kafka-serde-circe" % kafkaSerdeCirceVersion)

  }

  private[this] object TestDependencies {

    lazy val values: Seq[ModuleID] =
      (scalaTest ++ kafkaTestUtils).map(_ % Test)

    private lazy val scalaTest: Seq[ModuleID] = Seq("org.scalatest" %% "scalatest" % scalaTestVersion)

    private lazy val kafkaTestUtils =
      Seq("org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion, "jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.6")

  }
}
