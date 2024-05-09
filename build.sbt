ThisBuild / scalaVersion := "2.13.14"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.0",
      "dev.zio" %% "zio-json" % "0.6.2",
      "com.lihaoyi" %% "requests" % "0.8.2"
    )
  )
  .enablePlugins(JavaAppPackaging)
