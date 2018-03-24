package users

import cats.effect.Effect
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.{ dsl, HttpService, Response }
import org.http4s.server.middleware.Logger
import users.domain.{ Done, User }
import users.services.UserManagement
import users.services.usermanagement.Error, Error._
import users.util.json.instances._

package endpoints {

  private[endpoints] abstract class ServiceResult[F[_]: Effect, A] {
    def convert(a: A): F[Response[F]]
  }

  case class SignupUser(
      userName: String,
      emailAddress: String,
      password: Option[String]
  )

}

package object endpoints {

  ////////// Helper methods for generating responses

  private[endpoints] implicit def userResult[F[_]: Effect] = new ServiceResult[F, User] {
    val dslF = dsl.Http4sDsl[F]; import dslF._

    def convert(user: User) = Ok(user.asJson)
  }

  private[endpoints] implicit def doneResult[F[_]: Effect] = new ServiceResult[F, Done] {
    val dslF = dsl.Http4sDsl[F]; import dslF._

    def convert(user: Done) = NoContent()
  }

  private[endpoints] implicit def userListResult[F[_]: Effect] = new ServiceResult[F, List[User]] {
    val dslF = dsl.Http4sDsl[F]; import dslF._

    def convert(users: List[User]) = Ok(users.asJson)
  }

  private[endpoints] def generateResponse[F[_]: Effect, A: ServiceResult[F, ?]](
      user: Either[Error, A]
  ): F[Response[F]] =
    user match {
      case Right(res) ⇒ implicitly[ServiceResult[F, A]].convert(res)
      case Left(err)  ⇒ handleError(err)
    }

  private[endpoints] def handleError[F[_]: Effect](err: Error): F[Response[F]] = {
    val dslF = dsl.Http4sDsl[F]; import dslF.{ NotFound ⇒ _, _ }

    err match {
      case NotFound           ⇒ dslF.NotFound("User not found")
      case Exists             ⇒ Conflict("User with the same username already exists")
      case Active             ⇒ Conflict("User is already active")
      case Deleted            ⇒ Conflict("User is already deleted")
      case Blocked            ⇒ Conflict("User is already blocked")
      case System(underlying) ⇒ InternalServerError(underlying.toString)
    }
  }

  ////////// Different endpoints based on authentication level
  //////////
  ////////// It should be easy and straightforward to add auth using built-in Basic auth or Tsec if
  ////////// UserManagement supports `getByUsername` method, and if the User class has a `isAdmin`
  ////////// attribute.

  def admin[F[_]: Effect](service: UserManagement[F]): HttpService[F] =
    Logger[F](true, true)(new AdminEndpoint[F](service).value)

  def user[F[_]: Effect](service: UserManagement[F]): HttpService[F] =
    Logger[F](true, true)(new UserEndpoint[F](service).value)

  def unprevileged[F[_]: Effect](service: UserManagement[F]): HttpService[F] =
    Logger[F](true, true)(new UnprevilegedEndpoint[F](service).value)
}
