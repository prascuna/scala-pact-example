package application.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, Uri}
import akka.stream.ActorMaterializer
import application.config.ServiceConfig
import application.exceptions.DuplicatedEntryException
import application.models.MyData
import application.repositories.MyRepository
import application.routes.JsonSupport
import com.itv.scalapact.ScalaPactForger._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFunSpec, Matchers}

class MyServiceTest extends AsyncFunSpec with Matchers with MockitoSugar with JsonSupport {
  describe("MyService") {


    val servicePath = "/data"

    it("should be able to forward") {
      val data = MyData(Some(1), "sample data")
      val jsonData = myDataFormatter.write(data).toString
      val dataLocation = s"http://localhost:9093/data/${data.id.get}"

      forgePact
        .between("App2")
        .and("App3")
        .addInteraction(
          interaction
            .description("Forwarding the creation request")
            .given("Data does not already exist")
            .uponReceiving(
              method = POST,
              path = servicePath,
              query = None,
              headers = Map("Content-Type" -> "application/json"),
              body = Some(jsonData),
              matchingRules = None
            )
            .willRespondWith(
              status = 201,
              headers = Map("Location" -> dataLocation),
              body = None,
              matchingRules = None
            )
        )
        .runConsumerTest { mockConfig =>
          val serviceConfig = ServiceConfig(forwardMethod = HttpMethods.POST, forwardURI = Uri(s"${mockConfig.baseUrl}$servicePath"))
          implicit val system = ActorSystem("test-system")
          implicit val materializer = ActorMaterializer()

          val service = MyService(mock[MyRepository], serviceConfig)

          service.forward(data).map(locationURI => locationURI shouldBe Right(Uri(dataLocation)))

        }
    }
    it("should be returning an error in case of duplicated entry") {
      val data = MyData(Some(2), "duplicate data")
      val jsonData = myDataFormatter.write(data).toString
      val dataLocation = s"http://localhost:9093/data/${data.id.get}"

      forgePact
        .between("App2")
        .and("App3")
        .addInteraction(
          interaction
            .description("Forwarding the creation request")
            .given("Data already exists")
            .uponReceiving(
              method = POST,
              path = servicePath,
              query = None,
              headers = Map("Content-Type" -> "application/json"),
              body = Some(jsonData),
              matchingRules = None
            )
            .willRespondWith(
              status = 409,
              headers = Map("Location" -> dataLocation),
              body = None,
              matchingRules = None
            )
        )
        .runConsumerTest { mockConfig =>
          val serviceConfig = ServiceConfig(forwardMethod = HttpMethods.POST, forwardURI = Uri(s"${mockConfig.baseUrl}$servicePath"))
          implicit val system = ActorSystem("test-system")
          implicit val materializer = ActorMaterializer()

          val service = MyService(mock[MyRepository], serviceConfig)

          service.forward(data).map(res => res shouldBe Left(DuplicatedEntryException(Uri(dataLocation))))
        }
    }
  }

}
