package com.vanilla.poc.worker1

import java.net.InetAddress

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main extends App{
  val applicationName :String = "asset-manager-service"

  implicit val actorSystem: ActorSystem = ActorSystem(applicationName)
  actorSystem.actorOf(Props[HttpActorHolder],"actor-holder")
}

class HttpActorHolder extends Actor with ActorLogging with PlayJsonSupport{

  override def receive: Receive = Actor.emptyBehavior

  implicit val actorSystem: ActorSystem = context.system
  implicit val actorMatrializer: ActorMaterializer =  ActorMaterializer()
  implicit val fooFormat: Format[WorkerResponse] = Json.format[WorkerResponse]

  private val port = 8080
  private val interface = "0.0.0.0"
  val route: Route =

    get {
      path("response") {
        complete(getHost)
      }
    }

  val bindingFuture: Future[Http.ServerBinding] = Http()
    .bindAndHandle(route, interface, port)

  bindingFuture.foreach(_ => log.info(StringBuilder.newBuilder.append("worker1 started on interface ")
    .append(interface).append(" on port ").append(port).toString()))

  override def postStop(): Unit = {
    log.info("Stopping HttpHolderActor Actor!!!")
    bindingFuture
      .flatMap(_.unbind())
      .onComplete { _ =>
        actorMatrializer.shutdown()
      }
    log.info("Terminated... Bye")
  }

  def getHost:Future[WorkerResponse] = Future{
    val localhost = InetAddress.getLocalHost
    val localIpAddress = localhost.getHostAddress
    val hostName = localhost.getHostName
    val timeStamp = System.currentTimeMillis()
    WorkerResponse(hostName, localIpAddress,timeStamp)
  }
}

case class WorkerResponse(hostname:String, ipAdress:String,timestemp:Long)


