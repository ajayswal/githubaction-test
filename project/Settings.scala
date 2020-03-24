import sbt._

object Settings {

  val Version      = "0.0.30-5-SNAPSHOT"
  val ScalaVersion = "2.11.9"
  val resolvers: Seq[MavenRepository] = Seq(
    "Artifactory".at("https://repo.artifacts.weather.com/analytics-local/")
  )

  val credentials = Seq(
    Credentials(Path.userHome / ".sbt" / ".credentials")
  )

}
