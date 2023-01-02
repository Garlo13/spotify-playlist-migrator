
lazy val root =
  project.in(file("."))
    .settings(
      name := "spotify-playlist-migrator",
      version := "0.1",
      scalaVersion := "2.13.9",
      libraryDependencies := appDependencies
    )

lazy val appDependencies = Seq(
  "com.lihaoyi" %% "requests"     % "0.7.0",
  "io.circe"    %% "circe-parser" % "0.14.3",
)
