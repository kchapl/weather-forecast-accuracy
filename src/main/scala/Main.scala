package weather

import zio._
import zio.json._

import java.time._
import scala.math._

object Main extends ZIOAppDefault {

  private val baseUrl = "http://datapoint.metoffice.gov.uk/public/data"
  private val dataType = "json"

  private case class Site(id: String, name: String, latitude: String, longitude: String)
  private case class Location(Location: Seq[Site])
  private case class Locations(Locations: Location)

  private case class ObsParam(name: String, units: String, `$`: String)
  private case class ObsParams(Param: Seq[ObsParam])
  private case class ObsMetric(T: String)
  private case class ObsPeriod(`type`: String, value: String, Rep: Seq[ObsMetric])
  private case class ObsLocation(Period: Seq[ObsPeriod])
  private case class ObsData(Location: ObsLocation)
  private case class Obs(Wx: ObsParams, DV: ObsData)
  private case class ObsWrapper(SiteRep: Obs)

  private case class TimeTemperature(time: OffsetDateTime, temperature: Double)

  private implicit val siteDecoder: JsonDecoder[Site] = DeriveJsonDecoder.gen[Site]
  private implicit val locationDecoder: JsonDecoder[Location] = DeriveJsonDecoder.gen[Location]
  private implicit val locationsDecoder: JsonDecoder[Locations] = DeriveJsonDecoder.gen[Locations]

  private implicit val obsParamDecoder: JsonDecoder[ObsParam] = DeriveJsonDecoder.gen[ObsParam]
  private implicit val obsParamsDecoder: JsonDecoder[ObsParams] = DeriveJsonDecoder.gen[ObsParams]
  private implicit val obsMetricDecoder: JsonDecoder[ObsMetric] = DeriveJsonDecoder.gen[ObsMetric]
  private implicit val obsPeriodDecoder: JsonDecoder[ObsPeriod] = DeriveJsonDecoder.gen[ObsPeriod]
  private implicit val obsLocationDecoder: JsonDecoder[ObsLocation] =
    DeriveJsonDecoder.gen[ObsLocation]
  private implicit val obsDataDecoder: JsonDecoder[ObsData] = DeriveJsonDecoder.gen[ObsData]
  private implicit val obsDecoder: JsonDecoder[Obs] = DeriveJsonDecoder.gen[Obs]
  private implicit val obsWrapperDecoder: JsonDecoder[ObsWrapper] =
    DeriveJsonDecoder.gen[ObsWrapper]

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
    val x = period.Rep.zipWithIndex.minBy { case (y, _) => y.T.toDouble }
    val r = LocalDate.parse(period.value.stripSuffix("Z"))
    val t = LocalTime.of(x._2 + offset, 0)
    TimeTemperature(OffsetDateTime.of(r, t, ZoneOffset.UTC), x._1.T.toDouble)
  }

  private def minTemperature(location: ObsLocation): TimeTemperature = {
    val yst = location.Period.head
    val ystm = minTemperature(24 - yst.Rep.length)(yst)
    val tod = location.Period.last
    val todm = minTemperature(0)(tod)
    val x = Seq(ystm, todm)
    val y = x.minBy(u => u.temperature)
    y
  }

  private def env(name: String) = for {
    optValue <- System.env(name)
    value <- ZIO.fromOption(optValue).orElseFail(new RuntimeException(s"No $name in env"))
  } yield value

  private def program = for {
    apiKey <- env("API_KEY")
    latitude <- env("LATITUDE").map(_.toDouble)
    longitude <- env("LONGITUDE").map(_.toDouble)
    response1 <- ZIO.attempt(requests.get(s"$baseUrl/val/wxobs/all/$dataType/sitelist?key=$apiKey"))
    locations <- ZIO.fromEither(response1.text.fromJson[Locations])
    site = nearestSite(locations.Locations.Location, latitude, longitude)
    _ <- Console.printLine(site)
    response2 <- ZIO.attempt(
      requests.get(s"$baseUrl/val/wxobs/all/$dataType/${site.id}?res=hourly&key=$apiKey")
    )
    _ <- Console.printLine(response2.text)
    obs <- ZIO.fromEither(response2.text.fromJson[ObsWrapper])
    _ <- ZIO.foreachDiscard(obs.SiteRep.DV.Location.Period.flatMap(_.Rep).zipWithIndex) {
      case (x, y) =>
        Console.printLine(s"${y - 4}: $x")
    }
    _ <- Console.printLine(minTemperature(obs.SiteRep.DV.Location))
    _ <- Console.printLine("site,min,time,max,time")
  } yield ()

  override def run: ZIO[Any, Any, Unit] = program
}
