import sbt._

object Version {
  val CirceVersion             = "0.11.2"
  val Ficus                    = "1.4.7"
  val AkkaStreamKafka          = "1.0.5"
  val AkkaStream               = "2.5.25"
  val ScalaLogging             = "3.9.2"
  val LogbackClassic           = "1.2.3"
  val ScalaCheck               = "1.14.0"
  val CommonsCodec             = "1.6"
  val AkkaAvroParquet          = "1.1.1"
  val BarModelVersion          = "1.2.7-RELEASE"
  val PrometheusClient         = "0.6.0"
  val KafkaClientConfEnvReader = "0.1-2-SNAPSHOT"
  val TimeBucketVersion        = "0.0.1-SNAPSHOT"
}

object Dependencies {

  import Version._

  private val circe = Seq(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )

  val all = List(
    "com.weather"                %% "kafka-client-conf-env-reader"    % KafkaClientConfEnvReader,
    "com.typesafe.akka"          %% "akka-stream-kafka"               % AkkaStreamKafka,
    "com.iheart"                 %% "ficus"                           % Ficus,
    "com.weather"                %% "bar-model"                       % BarModelVersion,
    "com.typesafe.scala-logging" %% "scala-logging"                   % ScalaLogging,
    "com.typesafe.akka"          %% "akka-slf4j"                      % AkkaStream,
    "ch.qos.logback"             % "logback-classic"                  % LogbackClassic,
    "io.prometheus"              % "simpleclient"                     % PrometheusClient,
    "io.prometheus"              % "simpleclient_httpserver"          % PrometheusClient,
    "io.prometheus"              % "simpleclient_hotspot"             % PrometheusClient,
    "com.lightbend.akka"         %% "akka-stream-alpakka-avroparquet" % AkkaAvroParquet,
    "org.scalacheck"             %% "scalacheck"                      % ScalaCheck,
    "com.ibm.stocator"           % "stocator"                         % "1.0.35",
    "com.sksamuel.avro4s"        %% "avro4s-core"                     % "3.0.0-RC2",
    "org.apache.hadoop"          % "hadoop-minicluster"               % "3.2.0",
    "org.scalatest"              %% "scalatest"                       % "3.0.8" % Test,
    "org.apache.parquet"         % "parquet-avro"                     % "1.10.0", //Apache2
    ("org.apache.hadoop" % "hadoop-client" % "3.2.0").exclude("log4j", "log4j"), //Apache2
    ("org.apache.hadoop" % "hadoop-common" % "3.2.0").exclude("log4j", "log4j"), //Apache2
    "commons-codec" % "commons-codec"                       % CommonsCodec,
    "com.weather"   %% "analytics-ingest-time-bucket-model" % TimeBucketVersion
  ) ++ circe

}
