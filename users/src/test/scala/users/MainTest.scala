package users

import cats.effect._
import cats.implicits._
import java.time.OffsetDateTime
import io.circe.generic.semiauto._
import io.circe.{ Encoder, Json }
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.Uri
import scala.concurrent.{ duration, Await, Future }
import users.config._
import users.domain.User
import users.endpoints.SignupUser
import users.main._
import users.services.UserManagement
import users.util.json.instances._
import users.util.purity.instances._

import utest._

object MainTest extends TestSuite {
  ///// Test Prep
  System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR")

  var repo: UserManagement[Future] = _

  object TestRun extends ServerApp[IO] {
    val config = ApplicationConfig(
      executors = ExecutorsConfig(
        services = ExecutorsConfig.ServicesConfig(
          parallellism = 1
        )
      ),
      services = ServicesConfig(
        users = ServicesConfig.UsersConfig(
          failureProbability = 0,
          timeoutProbability = 0
        )
      )
    )
    val application = Application.fromApplicationConfig.run(config)
    repo = application.services.userManagement
  }

  val server = TestRun.builder.start.unsafeRunSync()
  val httpClient = Http1Client[IO]().unsafeRunSync()

  val target = Uri.uri("http://localhost:8080/users")

  //test data
  val testUsers = Vector(
    SignupUser("1", "1@example.com", None),
    SignupUser("2", "2@example.com", None),
    SignupUser("3", "3@example.com", None)
  )
  val storedUser: Array[User] = Array.fill(testUsers.length)(null)

  ///// Actual Tests

  val tests = Tests {
    'signUp - {

      val encoder: Encoder[SignupUser] = deriveEncoder

      //create request
      val requests = testUsers.map { u ⇒
        Request(
          method = Method.POST,
          uri = target,
          httpVersion = HttpVersion.`HTTP/1.1`,
          headers = Headers(), //Endpoint does not do strict header checking
          body = fs2
            .Stream(
              encoder(u).toString.getBytes(): _*
            )
            .covary[IO]
        )
      }

      // check username and email address
      requests.zipWithIndex.foreach {
        case (request, idx) ⇒
          val response = httpClient.expect[Json](request).unsafeRunSync()
          val userDec = response.as[User]
          assert(userDec.isRight)
          val user = userDec.right.get
          assert(user.userName.value === testUsers(idx).userName)
          assert(user.emailAddress.value === testUsers(idx).emailAddress)

          //For later use
          storedUser(idx) = user
      }
    }

    //Helper method for later tests
    def getByUsername(userName: String): User =
      httpClient
        .expect[Json](s"http://localhost:8080/users/${userName}")
        .unsafeRunSync()
        .as[User]
        .right
        .get

    'getByUsername - {
      testUsers.zipWithIndex.foreach {
        case (su, idx) ⇒
          val receivedUser = getByUsername(su.userName)

          assert(receivedUser.userName.value === su.userName)
          assert(receivedUser.emailAddress.value === su.emailAddress)
      }
    }

    'updateEmail - {
      def modifyEmail(original: String) = "modified-" + original

      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.POST,
            uri = target / receivedUser.id.value / "email",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2
              .Stream(
                modifyEmail(u.emailAddress).getBytes(): _*
              )
              .covary[IO]
          )

          val response = httpClient.expect[Json](request).unsafeRunSync()
          val userDec = response.as[User]
          assert(userDec.isRight)
          val user = userDec.right.get
          assert(user.emailAddress.value === modifyEmail(testUsers(idx).emailAddress))
      }
    }

    'updatePassword - {
      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val password = "password" + idx
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.POST,
            uri = target / receivedUser.id.value / "password",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2
              .Stream(
                password.getBytes(): _*
              )
              .covary[IO]
          )

          httpClient.expect[Json](request).unsafeRunSync()

          //Endpoint does not return password, so check manually from repo
          val userFromRepo = Await.result(repo.get(receivedUser.id), duration.Duration.Inf)
          assert(userFromRepo.isRight)
          assert(userFromRepo.right.get.password.get.value === password)
      }
    }

    'resetPassword - {
      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.DELETE,
            uri = target / receivedUser.id.value / "password",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2
              .Stream(
                "".getBytes(): _*
              )
              .covary[IO]
          )

          httpClient.expect[String](request).unsafeRunSync()

          //Endpoint does not return password, so check manually from repo
          val userFromRepo = Await.result(repo.get(receivedUser.id), duration.Duration.Inf)
          assert(userFromRepo.isRight)
          assert(userFromRepo.right.get.password.isEmpty)
      }

    }

    'delete - {
      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.DELETE,
            uri = target / receivedUser.id.value / "password",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2.Stream("".getBytes(): _*).covary[IO]
          )

          httpClient.expect[Json](request).unsafeRunSync()

          //Endpoint does not return password, so check manually from repo
          val userFromRepo = Await.result(repo.get(receivedUser.id), duration.Duration.Inf)
          assert(userFromRepo.isRight)
          assert(userFromRepo.right.get.password.isEmpty)
      }

    }

    'block - {
      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.POST,
            uri = target / receivedUser.id.value / "block",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2.Stream("".getBytes(): _*).covary[IO]
          )

          val response = httpClient.expect[Json](request).unsafeRunSync()
          val userDec = response.as[User]
          assert(userDec.isRight)
          val user = userDec.right.get
          val blockedTime = user.metadata.blockedAt

          assert(blockedTime.get isAfter OffsetDateTime.now().minusSeconds(3))
      }
    }

    'unblock - {
      testUsers.zipWithIndex.foreach {
        case (u, idx) ⇒
          val receivedUser = getByUsername(u.userName)

          val request = Request(
            method = Method.POST,
            uri = target / receivedUser.id.value / "unblock",
            httpVersion = HttpVersion.`HTTP/1.1`,
            headers = Headers(),
            body = fs2.Stream("".getBytes(): _*).covary[IO]
          )

          val response = httpClient.expect[Json](request).unsafeRunSync()
          val userDec = response.as[User]
          assert(userDec.isRight)
          val user = userDec.right.get
          val blockedStatus = user.metadata.blockedAt

          assert(blockedStatus.isEmpty)
      }
    }

    'all - {
      val request = Request(
        method = Method.GET,
        uri = target,
        httpVersion = HttpVersion.`HTTP/1.1`,
        headers = Headers(),
        body = fs2
          .Stream(
            "".getBytes(): _*
          )
          .covary[IO]
      )

      val allUsersDec = httpClient.expect[Json](request).unsafeRunSync().as[List[User]]
      assert(allUsersDec.isRight)
      //Server has other users registered too, from other test suites
      val usersFromServer = allUsersDec.right.get.map(u ⇒ u.id.value).toSet
      val localCompareTarget = storedUser.map(_.id.value).toSet

      assert(localCompareTarget subsetOf usersFromServer)
    }
  }

  ////// Cleanup
  override def utestAfterAll(): Unit =
    server.shutdownNow()
}
