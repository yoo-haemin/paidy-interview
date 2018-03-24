package users.endpoints

import cats.effect.Effect
import cats.implicits._
import org.http4s._
import users.domain.User
import users.services.UserManagement

class AdminEndpoint[F[_]: Effect](service: UserManagement[F]) {
  val value = {
    val dslF = dsl.Http4sDsl[F]
    import dslF._

    HttpService[F] {

      //Block(id)
      case POST -> Root / "users" / id / "block" ⇒
        service
          .block(User.Id(id))
          .flatMap(generateResponse[F, User])

      //Unblock(id)
      case POST -> Root / "users" / id / "unblock" ⇒
        service
          .unblock(User.Id(id))
          .flatMap(generateResponse[F, User])

      //All
      case GET -> Root / "users" ⇒
        service
          .all()
          .flatMap(generateResponse[F, List[User]])
    }
  }
}
