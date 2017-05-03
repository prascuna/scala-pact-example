import java.io.File

import com.whisk.docker.DockerReadyChecker.LogLineContains
import com.whisk.docker.{DockerContainer, DockerKit, VolumeMapping}

trait DockerMySQLService extends DockerKit {
  private val currentDir = new File(".").getCanonicalPath
  val mysqlContainer = DockerContainer("mysql:5.6")
    .withEnv(
      "MYSQL_ALLOW_EMPTY_PASSWORD=yes",
      "MYSQL_DATABASE=sampledb")
    .withPorts((3306, Some(3306)))
    .withVolumes(Seq(
      VolumeMapping(s"$currentDir/initdb", "/docker-entrypoint-initdb.d")
    ))
    .withReadyChecker(LogLineContains("socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server (GPL)"))

  abstract override def dockerContainers: List[DockerContainer] = mysqlContainer :: super.dockerContainers
}
