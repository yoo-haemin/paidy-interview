package users

import cats.~>
import cats.effect.{ Effect, IO }
import fs2.{ Stream, StreamApp }, StreamApp.ExitCode
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.Future
import users.config._
import users.main._
import users.util.purity._
import users.util.purity.instances._
import users.persistence.repositories._

object Main extends ServerApp[IO] with AppConfig

abstract class ServerApp[F[_]: Effect: Future ~> ?[_]] extends StreamApp[F] {
  val application: Application
  implicit lazy val ec = application.services.ec
  implicit lazy val service = application.services.userManagement.convert[F]

  lazy val builder =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(endpoints.admin(service), "/")
      .mountService(endpoints.user(service), "/")
      .mountService(endpoints.unprevileged(service), "/")

  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    builder.serve
}

trait AppConfig {
  val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.1,
        timeoutProbability = 0.1
      )
    )
  )

  val application = Application.fromApplicationConfig.run(config)
}
