package weather

import Main.env

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import zio._

import java.net.InetSocketAddress

// See https://github.com/softwaremill/simple-http-server/blob/master/src/main/scala/com/softwaremill/httpserver/SimpleHttpServer.scala
object Main2 extends ZIOAppDefault {

  private def createWebHook = for {
    apiKey <- env("WEB_HOOK_API_KEY")
    weatherReport <- Main.program.map(_.replace(",", "|||"))
    webHook =
      s"https://maker.ifttt.com/trigger/weather_reading/with/key/$apiKey?value1=$weatherReport&value2=&value3="
  } yield webHook

  private def startServer(port: Int, webHook: String) = ZIO.attempt {
    val server = HttpServer.create(new InetSocketAddress(port), 0)
    server.createContext("/", new RootHandler(webHook))
    server.setExecutor(null)
    server.start()
    server
  }

  private def program = ZIO.scoped(for {
    webHook <- createWebHook
    port <- env("PORT").map(_.toInt)
    _ <- ZIO.acquireRelease(startServer(port, webHook))(server => ZIO.succeed(server.stop(0)))
    _ <- ZIO.sleep(Duration.fromSeconds(30))
  } yield ())

  override def run: ZIO[Any, Any, Any] = program
}

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
