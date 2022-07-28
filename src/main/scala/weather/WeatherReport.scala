package weather

import weather.Observation._
import weather.Utils.env
import zio._

import scala.math._

object WeatherReport {

  private case class Position(latitude: Double, longitude: Double)

  private def distance(pos1: Position, pos2: Position) = sqrt(
    pow(abs(pos2.longitude - pos1.longitude), 2) + pow(abs(pos2.latitude - pos1.latitude), 2)
  )

  private def nearestSite(sites: Seq[Site], pos: Position) =
    sites.minBy(site => distance(Position(site.latitude.toDouble, site.longitude.toDouble), pos))

  def buildWeatherReport: RIO[MetOffice, String] = for {
    latitude <- env("LATITUDE").map(_.toDouble)
    longitude <- env("LONGITUDE").map(_.toDouble)
    now <- zio.Clock.currentDateTime
    locations <- MetOffice.fetchSiteList()
    site = nearestSite(locations.Locations.Location, Position(latitude, longitude))
    obs <- MetOffice.fetchSiteObservations(site.id)
    tts = TimeTemperature.temperatures(now, obs.SiteRep.DV.Location)
    _ <- ZIO.foreachDiscard(tts)(tt => Console.printLine(s"${tt.time}: ${tt.temperature}"))
    min = tts.minBy(_.temperature)
    max = tts.maxBy(_.temperature)
  } yield Seq(
    now,
    site.name,
    site.latitude,
    site.longitude,
    site.elevation,
    min.temperature,
    min.time,
    max.temperature,
    max.time
  ).mkString(",")
}
