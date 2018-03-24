package users.util

import cats.~>
import cats.effect._
import scala.concurrent.Future
import users.domain._
import users.services.UserManagement
import users.services.usermanagement.Error

package purity {
  class ServiceConverter[F[_]: Future ~> ?[_]] {
    def convert(underlying: UserManagement[Future])(implicit funK: Future ~> F): UserManagement[F] =
      new UserManagement[F] {
        import User._

        def generateId(): F[Id] = funK(underlying.generateId())

        def get(
            id: Id
        ): F[Error Either User] = funK(underlying.get(id))

        def getByUserName(
            userName: UserName
        ): F[Error Either User] = funK(underlying.getByUserName(userName))

        def signUp(
            userName: UserName,
            emailAddress: EmailAddress,
            password: Option[Password]
        ): F[Error Either User] = funK(underlying.signUp(userName, emailAddress, password))

        def updateEmail(
            id: Id,
            emailAddress: EmailAddress
        ): F[Error Either User] = funK(underlying.updateEmail(id, emailAddress))

        def updatePassword(
            id: Id,
            password: Password
        ): F[Error Either User] = funK(underlying.updatePassword(id, password))

        def resetPassword(
            id: Id
        ): F[Error Either User] = funK(underlying.resetPassword(id))

        def block(
            id: Id
        ): F[Error Either User] = funK(underlying.block(id))

        def unblock(
            id: Id
        ): F[Error Either User] = funK(underlying.unblock(id))

        def delete(
            id: Id
        ): F[Error Either Done] = funK(underlying.delete(id))

        def all(): F[Error Either List[User]] = funK(underlying.all())
      }
  }

  /** Instance for FunctionK[Future, IO], in separate scope to avoid collision
    */
  object instances {
    implicit val funK = new (Future ~> IO) {
      def apply[A](fa: Future[A]): IO[A] = IO.fromFuture(IO(fa))
    }
  }

}

package object purity {

  implicit class ServiceConversion(underlying: UserManagement[Future]) {
    def convert[F[_]: Future ~> ?[_]]: UserManagement[F] = (new ServiceConverter).convert(underlying)
  }

}
