package weather

import zio._
import zio.json._

import scala.math._

object Main extends ZIOAppDefault {

  private val baseUrl = "http://datapoint.metoffice.gov.uk/public/data"
  private val dataType = "json"

  private case class Site(id: String, name: String, latitude: String, longitude: String)
  private case class Location(Location: Seq[Site])
  private case class Locations(Locations: Location)

  private case class ObsParam(name: String, units: String, `$`: String)
  private case class ObsParams(Param: Seq[ObsParam])
  private case class ObsPeriod(`type`: String)
  private case class ObsLocation(i: String, Period: Seq[ObsPeriod])
  private case class ObsData(dataDate: String, Location: ObsLocation)
  private case class Obs(Wx: ObsParams, DV: ObsData)
  private case class ObsWrapper(SiteRep: Obs)

  private implicit val siteDecoder: JsonDecoder[Site] = DeriveJsonDecoder.gen[Site]
  private implicit val locationDecoder: JsonDecoder[Location] = DeriveJsonDecoder.gen[Location]
  private implicit val locationsDecoder: JsonDecoder[Locations] = DeriveJsonDecoder.gen[Locations]

  private implicit val obsParamDecoder: JsonDecoder[ObsParam] = DeriveJsonDecoder.gen[ObsParam]
  private implicit val obsParamsDecoder: JsonDecoder[ObsParams] = DeriveJsonDecoder.gen[ObsParams]
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
    _ <- Console.printLine(obs)
    _ <- Console.printLine("site,min,time,max,time")
  } yield ()

  override def run: ZIO[Any, Any, Unit] = program
}
