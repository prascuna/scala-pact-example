package application.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import application.config.ServiceConfig
import application.exceptions.{DuplicatedEntryException, LocationNotFoundException, MyException}
import application.models.MyData
import application.repositories.MyRepository
import application.routes.JsonSupport

import scala.concurrent.Future
import scala.util.Random

trait MyService {

  def write(data: MyData): Future[Either[MyException, MyData]]

  def read(id: Int): Future[Option[MyData]]

  def doSomething(data: MyData): MyData

  def forward(data: MyData): Future[Either[MyException, Uri]]

}

object MyService {
  def apply(myRepository: MyRepository, serviceConfig: ServiceConfig)(implicit system: ActorSystem, materializer: ActorMaterializer): MyService = new MyServiceImpl(myRepository, serviceConfig)
}

class MyServiceImpl(repository: MyRepository, serviceConfig: ServiceConfig)(implicit system: ActorSystem, materializer: ActorMaterializer) extends MyService with JsonSupport {

  override def doSomething(data: MyData): MyData =
    data.copy(name = data.name + " " + Random.nextInt())

  override def write(data: MyData): Future[Either[MyException, MyData]] =
    repository.write(data)

  override def read(id: Int): Future[Option[MyData]] =
    repository.read(id)

  override def forward(data: MyData): Future[Either[MyException, Uri]] = {
    implicit val ec = system.dispatcher

    Http().singleRequest(HttpRequest(
      method = serviceConfig.forwardMethod,
      uri = serviceConfig.forwardURI,
      entity = HttpEntity(ContentTypes.`application/json`, myDataFormatter.write(data).toString().toCharArray.map(_.toByte)
      )))
      .flatMap { httpResponse =>
        httpResponse.status match {
          case StatusCodes.Conflict =>
            Future.successful {
              httpResponse.headers.collectFirst {
                case Location(uri) => uri
              }.fold[Either[MyException, Uri]](Left(LocationNotFoundException)) { uri =>
                Left(DuplicatedEntryException(uri))
              }
            }
          case code if code.isSuccess() =>
            Future.successful {
              httpResponse.headers.collectFirst {
                case Location(uri) => uri
              }.fold[Either[MyException, Uri]](Left(LocationNotFoundException)) { uri =>
                Right(uri)
              }
            }

          case _ => Future.failed(new RuntimeException("Something else went wrong"))
        }
      }
  }

}
