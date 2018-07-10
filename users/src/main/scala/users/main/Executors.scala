package users.main

import cats.data.Reader
import org.zalando.grafter.macros.reader

import users.config._

import java.util.concurrent.ForkJoinPool
import scala.concurrent.ExecutionContext

@reader
final case class Executors(
    config: ExecutorsConfig
) {
  final val serviceExecutor: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool(config.services.parallellism))
}
