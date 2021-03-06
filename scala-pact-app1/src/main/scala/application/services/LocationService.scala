package application.services

import akka.http.scaladsl.model.Uri
import application.config.ServerConfig
import application.models.MyData

trait LocationService {

  def locate(daata: MyData): Uri

}

object LocationService {
  def apply(config: ServerConfig): LocationService = new LocationServiceImpl(config)
}

class LocationServiceImpl(config: ServerConfig) extends LocationService {
  val baseUrl = s"${config.scheme}://${config.host}:${config.port}"

  override def locate(data: MyData): Uri =
    Uri(s"$baseUrl/data/${data.id.get}")
}