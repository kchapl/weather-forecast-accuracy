ThisBuild / scalaVersion := "2.13.18"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.24",
      "dev.zio" %% "zio-json" % "0.8.0",
      "com.lihaoyi" %% "requests" % "0.9.1"
    )
  )
  .enablePlugins(JavaAppPackaging)
