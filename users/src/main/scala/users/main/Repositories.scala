package users.main

import cats.data.Reader
import org.zalando.grafter.macros.reader

import users.config._
import users.persistence.repositories._

@reader
final case class Repositories() {

  final val userRepository: UserRepository =
    UserRepository.inMemory()

}
