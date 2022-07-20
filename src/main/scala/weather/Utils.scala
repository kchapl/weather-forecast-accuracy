package weather

import zio.{System, ZIO}

object Utils {
  def env(name: String): ZIO[Any, RuntimeException, String] = for {
    optValue <- System.env(name)
    value <- ZIO.fromOption(optValue).orElseFail(new RuntimeException(s"No $name in env"))
  } yield value
}
