package weather

import weather.Distance.equirectangularApproximation
import weather.Observation._
import weather.Utils.env
import zio._

object WeatherReport {

  private def nearestSite(sites: Seq[Site], coord: Coordinate) =
    sites.minBy { site =>
      val siteCoord = Coordinate(site.latitude.toDouble, site.longitude.toDouble)
      equirectangularApproximation(siteCoord, coord)
    }

  def build: RIO[MetOffice, String] = for {
    latitude <- env("LATITUDE").map(_.toDouble)
    longitude <- env("LONGITUDE").map(_.toDouble)
    now <- zio.Clock.currentDateTime
    locations <- MetOffice.fetchSiteList()
    site = nearestSite(locations.Locations.Location, Coordinate(latitude, longitude))
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
