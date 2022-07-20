package weather

import requests.Response
import weather.Observation.{Locations, ObsWrapper}
import weather.Utils.env
import zio._
import zio.json._

trait MetOffice {
  def fetchSiteList(): ZIO[Any, Throwable, Locations]
  def fetchSiteObservations(siteId: String): ZIO[Any, Throwable, ObsWrapper]
}

object MetOffice {
  def fetchSiteList(): ZIO[MetOffice, Throwable, Locations] = ZIO.serviceWithZIO(_.fetchSiteList())
  def fetchSiteObservations(siteId: String): ZIO[MetOffice, Throwable, ObsWrapper] =
    ZIO.serviceWithZIO(_.fetchSiteObservations(siteId))
}

object MetOfficeLive {

  private val baseUrl = "http://datapoint.metoffice.gov.uk/public/data"
  private val dataType = "json"

  private def from[A](response: Response)(implicit decoder: JsonDecoder[A]): Task[A] =
    ZIO
      .fromEither(response.text().fromJson[A])
      .mapError(failure => new RuntimeException(s"Cannot parse [${response.text()}]: $failure"))

  val layer: ZLayer[Any, Throwable, MetOffice] = ZLayer.fromZIO(for {
    apiKey <- env("API_KEY")
  } yield new MetOffice {

    override def fetchSiteList(): ZIO[Any, Throwable, Locations] = for {
      response <- ZIO.attempt(
        requests.get(s"$baseUrl/val/wxobs/all/$dataType/sitelist?key=$apiKey")
      )
      locations <- from[Locations](response)
    } yield locations

    override def fetchSiteObservations(siteId: String): ZIO[Any, Throwable, ObsWrapper] = for {
      response <- ZIO.attempt(
        requests.get(s"$baseUrl/val/wxobs/all/$dataType/$siteId?res=hourly&key=$apiKey")
      )
      obs <- from[ObsWrapper](response)
    } yield obs
  })
}
