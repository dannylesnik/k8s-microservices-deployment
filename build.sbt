name := "k8s-microservices-deployment"

version := "0.1"

scalaVersion := "2.13.0"

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

lazy val orchestrator = (project in file("web-orchestrator"))
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(
    name := "orchestrator",
    DockerSettings.general,
    packageName in Docker := (dockerRepository in Docker).value+ "/"+name.value,
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
    packageName in Docker := (dockerRepository in Docker).value+ "/"+name.value,
    mainClass in Compile := Some("com.vanilla.poc.worker1.Main"),
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8",
    libraryDependencies += "de.heikoseeberger" %% "akka-http-play-json" % "1.27.0",
      libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23", 
      libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.4"

  )
