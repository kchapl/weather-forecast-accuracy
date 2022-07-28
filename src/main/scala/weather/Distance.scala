package weather

import scala.math.{cos, sqrt, toRadians}

object Distance {

  // Length of Earth's radius (km)
  private val r = 6371

  // Distance between coordinates on Earth (km)
  def equirectangularApproximation(coord1: Coordinate, coord2: Coordinate): Double = {
    val lat1 = toRadians(coord1.latitude)
    val lon1 = toRadians(coord1.longitude)
    val lat2 = toRadians(coord2.latitude)
    val lon2 = toRadians(coord2.longitude)
    val x = (lon2 - lon1) * cos((lat1 + lat2) / 2)
    val y = lat2 - lat1
    sqrt(x * x + y * y) * r
  }
}
