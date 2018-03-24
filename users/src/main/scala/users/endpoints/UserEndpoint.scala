package users.endpoints

import cats.effect.Effect
import cats.implicits._
import org.http4s._
import users.domain.{ Done, EmailAddress, Password, User, UserName }
import users.services.UserManagement

class UserEndpoint[F[_]: Effect](service: UserManagement[F]) {
  val value = {
    val dslF = dsl.Http4sDsl[F]
    import dslF._

    HttpService[F] {
      //get(id)
      case GET -> Root / "users" / userName ⇒
        service
          .getByUserName(UserName(userName))
          .flatMap(generateResponse[F, User])

      //updateEmail(id)
      case req @ POST -> Root / "users" / id / "email" ⇒
        req.decode[String] { email ⇒
          service
            .updateEmail(User.Id(id), EmailAddress(email))
            .flatMap(generateResponse[F, User])
        }

      //UpdatePassword(id)
      case req @ POST -> Root / "users" / id / "password" ⇒
        req.decode[String] { pass ⇒
          service
            .updatePassword(User.Id(id), Password(pass))
            .flatMap(generateResponse[F, User])
        }

      //ResetPassword(id)
      case DELETE -> Root / "users" / id / "password" ⇒
        service
          .resetPassword(User.Id(id))
          .flatMap(generateResponse[F, User])

      //Delete(id)
      case DELETE -> Root / "users" / id ⇒
        service
          .delete(User.Id(id))
          .flatMap(generateResponse[F, Done])
    }
  }
}
