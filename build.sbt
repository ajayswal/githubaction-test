import com.typesafe.sbt.packager.docker.DockerChmodType

lazy val `analytics-poc-ingest-parquet-writer` = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    name := "analytics-poc-ingest-parquet-writer",
    version := Settings.Version,
    scalaVersion := Settings.ScalaVersion,
    dockerBaseImage := "openjdk:jre-alpine",
    libraryDependencies ++= Dependencies.all,
    credentials ++= Settings.credentials,
    dockerExposedPorts := Seq(9095),
    resolvers ++= Settings.resolvers,
    dockerAdditionalPermissions += (DockerChmodType.UserGroupWriteExecute, "/opt/docker"),
    dockerExposedVolumes := Seq("/opt/docker/logs")
  )
  .settings(registryGitlabSettings)

val registryGitlabSettings = Seq(
  dockerRepository := Some("registry.gitlab.com"),
  dockerUsername := Some("nazarkisil91.nk/analytics-ingest-poc")
)

val registryGithubSettings = Seq(
  dockerRepository := Some("nk91")
)

val registryIBMSettings = Seq(
  dockerRepository := Some("us.icr.io"),
  dockerUsername := Some("icm-test-poc")
)
