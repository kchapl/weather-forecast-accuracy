ThisBuild / scalaVersion := "2.13.11"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-json" % "0.6.0",
      "com.lihaoyi" %% "requests" % "0.8.0"
    )
  )
  .enablePlugins(JavaAppPackaging)
