import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object BackendServer {
  def start(port: Int, message: String): Unit = {
    implicit val system: ActorSystem = ActorSystem(s"backend-server-$port")
    import system.dispatcher

    val route =
      path("health") {
        get {
          complete(StatusCodes.OK)
        }
      } ~
        path("") {
          get {
            complete(message)
          }
        }

    val bindingFuture = Http().newServerAt("localhost", port).bind(route)

    println(s"Backend server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}