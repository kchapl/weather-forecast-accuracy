package weather

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import zio._

import java.net.InetSocketAddress

// See https://github.com/softwaremill/simple-http-server/blob/master/src/main/scala/com/softwaremill/httpserver/SimpleHttpServer.scala
object Main2 extends ZIOAppDefault {

  private def createWebHook = for {
    weatherReport <- Main.program.map(_.replace(",", "|||"))
    webHook <- ZIO.attempt(s"TODO: $weatherReport")
  } yield webHook

  private def startServer(webHook: String) = ZIO.attempt {
    val server = HttpServer.create(new InetSocketAddress(8000), 0)
    server.createContext("/", new RootHandler(webHook))
    server.setExecutor(null)
    server.start()
    server
  }

  private def program = ZIO.scoped(for {
    webHook <- createWebHook
    _ <- ZIO.acquireRelease(startServer(webHook))(server => ZIO.succeed(server.stop(0)))
    _ <- Console.printLine("Hit any key to exit...")
    _ <- Console.readLine
  } yield ())

  override def run: ZIO[Any, Any, Any] = program
}

class RootHandler(webHook: String) extends HttpHandler {

  private def grabWebHook =
    Console.printLine(s"Making call out: [$webHook]")

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
