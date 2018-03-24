package users.endpoints

import cats.effect.Effect
import cats.implicits._
import org.http4s._
import org.http4s.circe.{ decodeUri ⇒ _, _ }
import io.circe._
import io.circe.generic.semiauto._
import users.domain._
import users.services.UserManagement

class UnprevilegedEndpoint[F[_]: Effect](service: UserManagement[F]) {

  def value = {
    val dslF = dsl.Http4sDsl[F]; import dslF._

    HttpService[F] {

      //signup
      case req @ POST -> Root / "users" ⇒
        implicit val suDecoder: Decoder[SignupUser] = deriveDecoder

        req.decodeWith(jsonOf, strict = false) { u ⇒
          service
            .signUp(UserName(u.userName), EmailAddress(u.emailAddress), u.password.map(Password(_)))
            .flatMap(generateResponse[F, User])
        }
    }
  }
}
