package weather

import com.sun.net.httpserver.HttpServer
import weather.Main.env
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
    port <- env("PORT").map(_.toInt)
    webHook <- createWebHook
    _ <- ZIO.acquireRelease(startServer(port, webHook))(server => ZIO.succeed(server.stop(0)))
    _ <- Console.readLine
  } yield ())

  override def run: ZIO[Any, Any, Any] = program
}
