package weather

import com.sun.net.httpserver.{HttpExchange, HttpHandler}
import zio._

// See https://github.com/softwaremill/simple-http-server/blob/master/src/main/scala/com/softwaremill/httpserver/SimpleHttpServer.scala
class RootHandler(webHook: String) extends HttpHandler {

  private def grabWebHook = for {
    _ <- Console.printLine(s"Making call out: [$webHook]")
    _ <- ZIO.attempt(requests.get(webHook))
  } yield ()

  private def sendResponse(exchange: HttpExchange) = ZIO.attempt {
    exchange.sendResponseHeaders(204, -1)
  }
  private def handleInternal(exchange: HttpExchange): Task[Unit] = for {
    _ <- grabWebHook
    _ <- sendResponse(exchange)
  } yield ()

  override def handle(exchange: HttpExchange): Unit =
    Unsafe.unsafe(implicit u =>
      Runtime.default.unsafe.run(handleInternal(exchange)).getOrThrowFiberFailure
    )
}
