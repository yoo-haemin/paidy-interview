package users.util.purity

import cats.data._
import cats.implicits._
import cats.effect.IO
import users.domain._
import users.persistence.repositories.UserRepository
import users.services.usermanagement.Interpreters
import users.util.purity.instances._
import scala.concurrent.ExecutionContext.Implicits.global
import users._
import utest._

object UserManagementIOTest extends TestSuite {
  val testService = Interpreters.default(UserRepository.inMemory()).convert[IO]

  val tests = Tests {
    'signUp - {
      val testUserName = UserName("a")
      val testEmail = EmailAddress("a@example.com")
      val testPassword = None

      val run = for {
        signedUpUser ← testService.signUp(testUserName, testEmail, testPassword)
        _ = assert(signedUpUser.isRight)
        getUser ← testService.getByUserName(testUserName)
        _ = assert(getUser.isRight)
        _ = assert(signedUpUser.right.get == getUser.right.get)
      } yield ()

      run.unsafeRunSync()
    }

  }

}
