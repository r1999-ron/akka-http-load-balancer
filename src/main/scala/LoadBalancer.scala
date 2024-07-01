import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object LoadBalancer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("load-balancer")
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = Timeout(5.seconds)

    val backendServers = List("http://localhost:8080", "http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
    val checkInterval = 10.seconds
    val loadBalancerActor = system.actorOf(Props(new LoadBalancerActor(backendServers, checkInterval)), "loadBalancerActor")

    val route =
      path("health") {
        get {
          val responseFuture: Future[HttpResponse] = (loadBalancerActor ? LoadBalancerActor.ForwardRequest(HttpRequest())).mapTo[HttpResponse]
          complete(responseFuture)
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8084).bind(route)

    println(s"Load balancer online at http://localhost:8084/health\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}