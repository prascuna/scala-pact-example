import application.Main
import application.config.AppConfig
import application.models.MyData
import application.repositories.MyRepository
import com.itv.scalapact.ScalaPactVerify._
import com.typesafe.config.ConfigFactory
import com.whisk.docker.impl.dockerjava.DockerKitDockerJava
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.{BeforeAndAfterAll, FunSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class PactVerifySpec extends FunSpec with BeforeAndAfterAll with DockerTestKit with DockerKitDockerJava with DockerMySQLService {

  //  var bidings: Http.ServerBinding = _

  override def beforeAll(): Unit = {
    println("before - 1")
    super.beforeAll()
    Main.startServer { serverBindings =>
      println(s"Server Started at ${serverBindings.localAddress}")
    }
    println("before - 2")
  }

  override def afterAll(): Unit = {
    println("after - 1")
    super.afterAll()
    //    bidings.unbind()
    println("after - 2")
  }

  val config = new AppConfig(ConfigFactory.load())
  val myRepository = MyRepository()
  describe("Verifying Consumer Contracts") {
    it("should be able to verify its contracts") {
      verifyPact
        .withPactSource(pactBroker("http://localhost", "App3", List("App2")))
        .setupProviderState("given") {
          case "Data does not already exist" => Await.ready(
            for {
              _ <- myRepository.truncate
            } yield (),
            5 seconds)
            true
          case "Data already exists" => Await.ready(
            for {
              _ <- myRepository.truncate
              _ <- myRepository.write(MyData(2, "duplicate data"))
            } yield (),
            5 seconds)
            true
        }
        .runVerificationAgainst("localhost", config.serverConfig.port)
    }
  }

}
