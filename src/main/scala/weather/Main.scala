package weather

import weather.Observation._
import weather.Utils.env
import zio._

import scala.math._

object Main {

  private def distance(
      fromLatitude: Double,
      fromLongitude: Double,
      toLatitude: Double,
      toLongitude: Double
  ) = sqrt(pow(abs(toLongitude - fromLongitude), 2) + pow(abs(toLatitude - fromLatitude), 2))

  private def nearestSite(sites: Seq[Site], latitude: Double, longitude: Double) =
    sites.minBy(site =>
      distance(site.latitude.toDouble, site.longitude.toDouble, latitude, longitude)
    )

  def program: RIO[MetOffice, String] = for {
    latitude <- env("LATITUDE").map(_.toDouble)
    longitude <- env("LONGITUDE").map(_.toDouble)
    now <- zio.Clock.currentDateTime
    locations <- MetOffice.fetchSiteList()
    site = nearestSite(locations.Locations.Location, latitude, longitude)
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
