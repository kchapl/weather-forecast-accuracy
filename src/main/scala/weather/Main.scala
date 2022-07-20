package weather

import weather.Observation._
import weather.Utils.env
import zio._

import java.time._
import scala.math._

object Main {

  case class TimeTemperature(time: OffsetDateTime, temperature: Double)

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

  private def minTemperature(offset: Int)(period: ObsPeriod) = {
    val x = period.Rep.zipWithIndex.reverse.minBy { case (y, _) => y.T.toDouble }
    toTimeTemperature(x._1, x._2, period.value, offset)
  }

  private def maxTemperature(offset: Int)(period: ObsPeriod) = {
    val x = period.Rep.zipWithIndex.reverse.maxBy { case (y, _) => y.T.toDouble }
    toTimeTemperature(x._1, x._2, period.value, offset)
  }

  private def toTimeTemperature(metric: ObsMetric, hour: Int, dateStr: String, offset: Int) = {
    val r = LocalDate.parse(dateStr.stripSuffix("Z"))
    val t = LocalTime.of(hour + offset, 0)
    TimeTemperature(OffsetDateTime.of(r, t, ZoneOffset.UTC), metric.T.toDouble)
  }

  private def minTemperature(location: ObsLocation): TimeTemperature = {
    val yst = location.Period.head
    val ystm = minTemperature(24 - yst.Rep.length)(yst)
    val tod = location.Period.last
    val todm = minTemperature(0)(tod)
    val x = Seq(ystm, todm)
    x.reverse.minBy(_.temperature)
  }

  private def maxTemperature(location: ObsLocation): TimeTemperature = {
    val yst = location.Period.head
    val ystm = maxTemperature(24 - yst.Rep.length)(yst)
    val tod = location.Period.last
    val todm = maxTemperature(0)(tod)
    val x = Seq(ystm, todm)
    x.reverse.maxBy(_.temperature)
  }

  def program: RIO[MetOffice, String] = for {
    latitude <- env("LATITUDE").map(_.toDouble)
    longitude <- env("LONGITUDE").map(_.toDouble)
    locations <- MetOffice.fetchSiteList()
    site = nearestSite(locations.Locations.Location, latitude, longitude)
    obs <- MetOffice.fetchSiteObservations(site.id)
    _ <- ZIO.foreachDiscard(obs.SiteRep.DV.Location.Period.flatMap(_.Rep).zipWithIndex) {
      case (x, y) =>
        Console.printLine(s"${y - 4}: $x")
    }
    min = minTemperature(obs.SiteRep.DV.Location)
    max = maxTemperature(obs.SiteRep.DV.Location)
  } yield s"${site.name},${site.latitude},${site.longitude},${site.elevation},${min.temperature},${min.time},${max.temperature},${max.time}"
}
