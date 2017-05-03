package application

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import application.config.AppConfig
import application.repositories.{FlywayRunner, MyRepository}
import application.routes.{RoutesAggregator, SampleRoute}
import application.services.{LocationService, MyService}
import com.typesafe.config.ConfigFactory


object Main {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val appConfig = new AppConfig(ConfigFactory.load())

  FlywayRunner(appConfig.sampleDbConfig).migrate

  lazy val repository = MyRepository()
  lazy val service = MyService(repository, appConfig.serviceConfig)
  lazy val locationService = LocationService(appConfig.serverConfig)


  val routes = RoutesAggregator(
    SampleRoute(service, locationService)
  )

  def main(args: Array[String]) = {
    startServer(serverBindings => println(s"Server Started at ${serverBindings.localAddress}"))
  }

  def startServer[T](bindingFunction: (ServerBinding) => T) =
    Http().bindAndHandle(routes, appConfig.serverConfig.host, appConfig.serverConfig.port)
      .foreach { serverBindings =>
        println(s"Server Started at ${serverBindings.localAddress}")
        bindingFunction(serverBindings)
      }

  //    Http().bindAndHandle(routes, appConfig.serverConfig.host, appConfig.serverConfig.port)
  //      .foreach(serverBindings => println(s"Server Started at ${serverBindings.localAddress}"))
}
