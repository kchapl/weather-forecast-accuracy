ThisBuild / scalaVersion := "2.13.8"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "com.lihaoyi" %% "requests" % "0.8.0"
    )
  )
  .enablePlugins(JavaAppPackaging)
