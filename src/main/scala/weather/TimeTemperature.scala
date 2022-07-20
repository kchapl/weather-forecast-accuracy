package weather

import weather.Observation.ObsLocation

import java.time.OffsetDateTime
import scala.annotation.tailrec

case class TimeTemperature(time: OffsetDateTime, temperature: Double)

object TimeTemperature {

  def temperatures(now: OffsetDateTime, location: ObsLocation): Seq[TimeTemperature] = {

    @tailrec
    def go(
        ts: Seq[Double],
        acc: Seq[TimeTemperature],
        lastTime: OffsetDateTime
    ): Seq[TimeTemperature] =
      ts match {
        case hd :: tl =>
          val time = lastTime.minusHours(1)
          go(tl, acc :+ TimeTemperature(time, hd), time)
        case Nil => acc
      }

    go(
      location.Period.flatMap(_.Rep.map(_.T.toDouble)).tail.reverse,
      Nil,
      now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
    ).reverse
  }
}
