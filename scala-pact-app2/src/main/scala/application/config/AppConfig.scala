package application.config

import akka.http.scaladsl.model.{HttpMethod, HttpMethods, Uri}
import com.typesafe.config.Config


class AppConfig(config: Config) {

  import AppConfig._

  val serverConfig = ServerConfig(
    scheme = config.getString("app.scheme"),
    host = config.getString("app.host"),
    port = config.getInt("app.port")
  )

  val serviceConfig = ServiceConfig(
    forwardMethod = config.getHttpMethod("service.forwardMethod"),
    forwardURI = config.getURI("service.forwardURI")
  )

  val sampleDbConfig = DbConfig(
    uri = config.getURI("sampledb.url"),
    user = config.getString("sampledb.user"),
    password = config.getString("sampledb.password")
  )
}

object AppConfig {

  implicit class RichConfig(config: Config) {
    def getURI(path: String): Uri = Uri(config.getString(path))

    def getHttpMethod(path: String): HttpMethod =
      config.getString(path) match {
        case "CONNECT" => HttpMethods.CONNECT
        case "DELETE" => HttpMethods.DELETE
        case "GET" => HttpMethods.GET
        case "HEAD" => HttpMethods.HEAD
        case "OPTIONS" => HttpMethods.OPTIONS
        case "PATCH" => HttpMethods.PATCH
        case "POST" => HttpMethods.POST
        case "PUT" => HttpMethods.PUT
        case "TRACE" => HttpMethods.TRACE
      }
  }

}


case class ServerConfig(scheme: String, host: String, port: Int)

case class ServiceConfig(forwardMethod: HttpMethod, forwardURI: Uri)

case class DbConfig(uri: Uri, user: String, password: String)