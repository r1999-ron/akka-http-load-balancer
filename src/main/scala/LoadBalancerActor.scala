import akka.actor.{Actor, ActorSystem, Props, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class LoadBalancerActor(backendServers: List[String], checkInterval: FiniteDuration)(implicit ec: ExecutionContext, system: ActorSystem) extends Actor {
  import LoadBalancerActor._
  import context.dispatcher

  var activeServers: List[String] = List.empty // Initially no servers are active
  var nextServerIndex: Int = 0 // Index for round-robin selection

  // Schedule periodic health checks
  context.system.scheduler.scheduleWithFixedDelay(0.seconds, checkInterval, self, HealthCheck)

  override def preStart(): Unit = {
    // Perform initial health checks and mark servers as active
    backendServers.foreach { server =>
      val healthCheckUri = s"$server/health"
      Http().singleRequest(HttpRequest(uri = healthCheckUri))
        .flatMap { response =>
          response.status match {
            case StatusCodes.OK =>
              self ! AddServer(server) // Mark server as active if health check is successful
              Future.successful(())
            case _ =>
              self ! RemoveServer(server) // Mark server as inactive if health check fails
              Future.failed(new RuntimeException(s"Health check failed for $healthCheckUri"))
          }
        }
        .recover {
          case ex =>
            self ! RemoveServer(server) // Mark server as inactive if health check throws an exception
            throw new RuntimeException(s"Health check failed for $healthCheckUri", ex)
        }
    }
  }

  def receive: Receive = {
    case ForwardRequest(request) =>
      if (activeServers.isEmpty) {
        sender() ! HttpResponse(StatusCodes.ServiceUnavailable, entity = "No available servers.")
      } else {
        val server = activeServers(nextServerIndex)
        nextServerIndex = (nextServerIndex + 1) % activeServers.size // Update next server index
        forwardRequestToServer(request, sender(), server)
      }

    case HealthCheck =>
      activeServers.foreach { server =>
        val healthCheckUri = s"$server/health"
        Http().singleRequest(HttpRequest(uri = healthCheckUri))
          .flatMap { response =>
            response.status match {
              case StatusCodes.OK =>
                self ! AddServer(server)
                Future.successful(())
              case _ =>
                self ! RemoveServer(server)
                Future.failed(new RuntimeException(s"Health check failed for $healthCheckUri"))
            }
          }
          .recover {
            case ex =>
              self ! RemoveServer(server)
              throw new RuntimeException(s"Health check failed for $healthCheckUri", ex)
          }
      }

    case AddServer(server) =>
      if (!activeServers.contains(server)) {
        activeServers = activeServers :+ server
        context.system.log.info(s"Server $server is now active. Active servers: $activeServers")
      }

    case RemoveServer(server) =>
      activeServers = activeServers.filterNot(_ == server)
      context.system.log.info(s"Server $server is now inactive. Active servers: $activeServers")
  }

  private def forwardRequestToServer(request: HttpRequest, replyTo: ActorRef, server: String): Unit = {
    val backendServerUrl = server
    val backendResponse = Http().singleRequest(request.copy(uri = Uri(backendServerUrl)))
    backendResponse.onComplete {
      case Success(response) =>
        if (response.status.isSuccess()) {
          replyTo ! response
        } else {
          // Retry with the next server if current server fails
          forwardRequestToServer(request, replyTo, activeServers(nextServerIndex))
        }
      case Failure(_) =>
        // Immediately mark server as inactive on failure
        self ! RemoveServer(server)
        forwardRequestToServer(request, replyTo, activeServers(nextServerIndex))
    }
  }
}

object LoadBalancerActor {
  case class ForwardRequest(request: HttpRequest)
  case class AddServer(server: String)
  case class RemoveServer(server: String)
  case object HealthCheck
}