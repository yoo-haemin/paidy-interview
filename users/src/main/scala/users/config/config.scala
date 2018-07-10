package users.config

import org.zalando.grafter.macros.readers
import cats.data._

@readers
case class ApplicationConfig(
    executors: ExecutorsConfig,
    services: ServicesConfig
)

case class ExecutorsConfig(
    services: ExecutorsConfig.ServicesConfig
)

object ExecutorsConfig {
  case class ServicesConfig(
      parallellism: Int
  )
}

case class ServicesConfig(
    users: ServicesConfig.UsersConfig
)

object ServicesConfig {
  val fromApplicationConfig: Reader[ApplicationConfig, ServicesConfig] =
    Reader(_.services)

  case class UsersConfig(
      failureProbability: Double,
      timeoutProbability: Double
  )
}
