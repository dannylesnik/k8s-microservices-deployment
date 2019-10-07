import scala.sys.process._
import sbt._
import Keys._
import scala.language.postfixOps


name := "k8s-microservices-deployment"

version in ThisBuild := sys.env.getOrElse("BUILD_NUMBER", "0.3.3.3-SNAPSHOT")

skip in publish := true

scalaVersion := "2.13.0"

lazy val kustomizeWorker = taskKey[Unit]("Update latest Image version :)")
lazy val kustomizeOrchestator = taskKey[Unit]("Update latest Image version :)")

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

lazy val orchestrator = (project in file("web-orchestrator"))
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(
    name := "orchestrator",
    DockerSettings.general,
    packageName in Docker := "113379206287.dkr.ecr.us-east-1.amazonaws.com/development/codebuild-poc/"+name.value,
    kustomizeOrchestator :=  { ("kustomize edit set image " +  (packageName in Docker).value + "="+(packageName in Docker).value+":"+version.value) #&& ("kustomize build . -o deployment/deployment.yaml") ! },
    (publish in Docker) := ((publish in Docker) dependsOn kustomizeOrchestator).value,
    libraryDependencies ++=  Seq(
      guice,
      ws
    )
  )


lazy val worker = (project in file("web-worker"))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "worker",
    DockerSettings.general,
    packageName in Docker := "113379206287.dkr.ecr.us-east-1.amazonaws.com/development/codebuild-poc/"+name.value,
    kustomizeWorker :=  { ("kustomize edit set image " +  (packageName in Docker).value + "="+(packageName in Docker).value+":"+version.value) #&& ("kustomize build . -o deployment/deployment.yaml") ! },
    (publish in Docker) := ((publish in Docker) dependsOn kustomizeWorker).value,
    mainClass in Compile := Some("com.vanilla.poc.worker1.Main"),
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8",
    libraryDependencies += "de.heikoseeberger" %% "akka-http-play-json" % "1.27.0",
      libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23", 
      libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.4"
  )
