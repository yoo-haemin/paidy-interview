package users.main

import cats.data._
import org.zalando.grafter.macros.reader

import users.config._
import users.services._

import scala.concurrent.Future

@reader
final case class Services(
    config: ServicesConfig,
    executors: Executors,
    repositories: Repositories
) {
  import executors._
  import repositories._

  implicit val ec = serviceExecutor

  final val userManagement: UserManagement[Future[?]] =
    UserManagement.unreliable(
      UserManagement.default(userRepository),
      config.users
    )
}
