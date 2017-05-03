import application.Main
import application.config.AppConfig
import com.itv.scalapact.ScalaPactVerify._
import com.typesafe.config.ConfigFactory
import com.whisk.docker.impl.dockerjava.DockerKitDockerJava
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.{BeforeAndAfterAll, FunSpec}

class PactVerifySpec extends FunSpec with BeforeAndAfterAll with DockerTestKit with DockerKitDockerJava with DockerMySQLService {

  //  var bidings: Http.ServerBinding = _

  override def beforeAll(): Unit = {
    println("before - 1")
    super.beforeAll()
    Main.startServer { serverBindings =>
      //      bidings = serverBindings
    }
    println("before - 2")
  }

  override def afterAll(): Unit = {
    println("after - 1")
    super.afterAll()
    //    bidings.unbind()
    println("after - 2")
  }

//  val myRepository = MyRepository()
  val config = new AppConfig(ConfigFactory.load())
  describe("Verifying Consumer Contracts") {
    it("should be able to verify its contracts") {
      verifyPact
        .withPactSource(pactBroker("http://localhost", "App2", List("App1")))
        .noSetupRequired
        .runVerificationAgainst("localhost", config.serverConfig.port)
    }
  }

}
