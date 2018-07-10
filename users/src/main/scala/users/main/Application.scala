package users.main

import cats.data._
import users.config._

import org.zalando.grafter.macros.reader

@reader
case class Application(
    services: Services
)
