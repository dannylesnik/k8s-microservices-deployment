package controllers

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong
import akka.actor.ActorSystem
import com.google.inject.Inject
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class MainController @Inject()(ws: WSClient,cc: ControllerComponents,actorSystem: ActorSystem) extends AbstractController(cc) {

  implicit val workerResponseReads: Reads[WorkerResponse] = Json.reads[WorkerResponse]
  implicit val cookieWrites: OWrites[WorkerResponse] = Json.writes[WorkerResponse]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

  private val worker1Url = actorSystem.settings.config.getString("app.worker1")
  private val worker2Url = actorSystem.settings.config.getString("app.worker2")
  private val worker3Url = actorSystem.settings.config.getString("app.worker3")
  private val worker4Url = actorSystem.settings.config.getString("app.worker4")

  def isAlive: Action[AnyContent] =Action.async{
    Future{
      val localhost = InetAddress.getLocalHost
      val localIpAddress = localhost.getHostAddress
      val hostName = localhost.getHostName
      Ok(localIpAddress +" - "+ hostName+" "+System.currentTimeMillis())
    }
  }

  def getResponse: Action[AnyContent] =Action.async{
    val timeBefore = new AtomicLong(System.currentTimeMillis())

    val yieldResult = for{
      worker1 <- ws.url(worker1Url).get().map(response => response.json.validate[WorkerResponse]).map("worker1" -> _.get)
      worker2 <- ws.url(worker2Url).get().map(response => response.json.validate[WorkerResponse]).map("worker2" -> _.get)
      worker3 <- ws.url(worker3Url).get().map(response => response.json.validate[WorkerResponse]).map("worker3" -> _.get)
      worker4 <- ws.url(worker4Url).get().map(response => response.json.validate[WorkerResponse]).map("worker4" -> _.get)

    } yield Seq(worker1,worker2,worker3,worker4).toMap
    yieldResult.map (response => Ok(Json.toJson(Response(System.currentTimeMillis() - timeBefore.get(),response))))
  }
}

case class WorkerResponse(hostname:String, ipAdress:String,timestemp:Long)
case class Response(took:Long,results:Map[String, WorkerResponse])
