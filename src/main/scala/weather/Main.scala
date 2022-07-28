package weather

import com.sun.net.httpserver.HttpServer
import weather.Utils.env
import zio._

import java.net.InetSocketAddress

// See https://github.com/softwaremill/simple-http-server/blob/master/src/main/scala/com/softwaremill/httpserver/SimpleHttpServer.scala
object Main extends ZIOAppDefault {

  private def startServer(port: Int, webHookApiKey: String) = for {
    server <- ZIO.attempt(HttpServer.create(new InetSocketAddress(port), 0))
    _ <- ZIO.attempt(server.createContext("/", new RootHandler(webHookApiKey)))
    _ <- ZIO.attempt(server.setExecutor(null))
    _ <- ZIO.attempt(server.start())
  } yield server

  private def program = ZIO.scoped(for {
    port <- env("PORT").map(_.toInt)
    webHookApiKey <- env("WEB_HOOK_API_KEY")
    _ <- startServer(port, webHookApiKey)
    _ <- Console.printLine(s"Server started on port $port ...")
    _ <- ZIO.never
  } yield ())

  override def run: ZIO[Any, Any, Any] = program
}
