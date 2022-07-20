package weather

import zio.json._

object Observation {

  case class Site(id: String, name: String, latitude: String, longitude: String, elevation: String)

  object Site {
    implicit val decoder: JsonDecoder[Site] = DeriveJsonDecoder.gen[Site]
  }

  case class Location(Location: Seq[Site])

  object Location {
    implicit val decoder: JsonDecoder[Location] = DeriveJsonDecoder.gen[Location]
  }

  case class Locations(Locations: Location)

  object Locations {
    implicit val decoder: JsonDecoder[Locations] = DeriveJsonDecoder.gen[Locations]
  }

  case class ObsParam(name: String, units: String, `$`: String)

  object ObsParam {
    implicit val decoder: JsonDecoder[ObsParam] = DeriveJsonDecoder.gen[ObsParam]
  }

  case class ObsParams(Param: Seq[ObsParam])

  object ObsParams {
    implicit val decoder: JsonDecoder[ObsParams] = DeriveJsonDecoder.gen[ObsParams]
  }

  case class ObsMetric(T: String)

  object ObsMetric {
    implicit val decoder: JsonDecoder[ObsMetric] = DeriveJsonDecoder.gen[ObsMetric]
  }

  case class ObsPeriod(`type`: String, value: String, Rep: Seq[ObsMetric])

  object ObsPeriod {
    implicit val decoder: JsonDecoder[ObsPeriod] = DeriveJsonDecoder.gen[ObsPeriod]
  }

  case class ObsLocation(Period: Seq[ObsPeriod])

  object ObsLocation {
    implicit val decoder: JsonDecoder[ObsLocation] = DeriveJsonDecoder.gen[ObsLocation]
  }

  case class ObsData(Location: ObsLocation)

  object ObsData {
    implicit val decoder: JsonDecoder[ObsData] = DeriveJsonDecoder.gen[ObsData]
  }

  case class Obs(Wx: ObsParams, DV: ObsData)

  object Obs {
    implicit val decoder: JsonDecoder[Obs] = DeriveJsonDecoder.gen[Obs]
  }

  case class ObsWrapper(SiteRep: Obs)

  object ObsWrapper {
    implicit val decoder: JsonDecoder[ObsWrapper] = DeriveJsonDecoder.gen[ObsWrapper]
  }
}
