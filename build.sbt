ThisBuild / scalaVersion := "2.13.16"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.20",
      "dev.zio" %% "zio-json" % "0.7.44",
      "com.lihaoyi" %% "requests" % "0.9.0"
    )
  )
  .enablePlugins(JavaAppPackaging)
