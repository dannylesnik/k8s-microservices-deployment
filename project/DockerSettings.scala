import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerBuildOptions, _}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{daemonGroup, daemonUser}
import sbt.Def
import sbt.Keys.version
import com.typesafe.sbt.packager.docker.DockerChmodType

object DockerSettings {

  lazy val general=  Seq(
    dockerBuildOptions += "--no-cache",
    version in Docker := version.value,
    daemonUser in Docker := "root",
    dockerChmodType in Docker := DockerChmodType.UserGroupWriteExecute,
    dockerChmodType := DockerChmodType.UserGroupWriteExecute,
      daemonGroup := "root",
    dockerBaseImage :=  "openjdk:11.0.3-jdk-slim-stretch"
  )
}
