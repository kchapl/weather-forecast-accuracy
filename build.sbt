ThisBuild / scalaVersion := "2.13.14"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Wunused", "-Werror")

lazy val root = (project in file("."))
  .settings(
    name := "WeatherForecastAccuracy",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.2",
      "dev.zio" %% "zio-json" % "0.7.0",
      "com.lihaoyi" %% "requests" % "0.8.3"
    )
  )
  .enablePlugins(JavaAppPackaging)
