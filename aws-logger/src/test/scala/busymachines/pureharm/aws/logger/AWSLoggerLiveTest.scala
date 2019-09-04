/**
  * Copyright (c) 2017-2019 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.pureharm.aws.logger

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import org.scalatest.funsuite.AnyFunSuite
import scala.concurrent.duration._

/**
  * Before running this ensure that you actually have the proper local environment
  * variables. See the ``pureharm-aws/aws-logger/src/test/resources/reference.conf``
  * for the environment variables that are used by this test.
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04 Sep 2019
  *
  */
@org.scalatest.Ignore
final class AWSLoggerLiveTest extends AnyFunSuite {

  private val ioRuntime: Later[(ContextShift[IO], Timer[IO])] = IORuntime.defaultMainRuntime("s3-cf-test")

  implicit private val cs:    ContextShift[IO] = ioRuntime.value._1
  implicit private val timer: Timer[IO]        = ioRuntime.value._2

  private val fixture: Resource[IO, AWSLoggerFactory[IO]] = for {
    blockingEC <- Pools.cached[IO]("aws-logger-block")
    implicit0(b: BlockingShifter[IO]) <- BlockingShifter.fromExecutionContext[IO](blockingEC).pure[Resource[IO, ?]]
    config  <- AWSLoggerConfig.fromNamespaceR[IO]("test-live.pureharm.aws.logger")
    _       <- Resource.liftF(IO(println(config)))
    logFact <- AWSLoggerFactory.resource[IO](config)
  } yield logFact

  test("... should send logs to AWS cloud") {
    val testIO = fixture.use { loggerFactory: AWSLoggerFactory[IO] =>
      for {
        localLogger  <- Slf4jLogger.create[IO]
        randomNumber <- IO(Math.abs(scala.util.Random.nextLong()))
        _            <- localLogger.info(s"LOCAL — look manually for number '$randomNumber' in aws cloudwatch logs")
        logger       <- loggerFactory.getLogger(localLogger).pure[IO]
        _            <- logger.trace(s"pureharm — logger test $randomNumber — trace")
        _            <- logger.debug(s"pureharm — logger test $randomNumber — debug")
        _            <- logger.info(s"pureharm — logger test $randomNumber — info")
        _            <- logger.warn(s"pureharm — logger test $randomNumber — warn")
        _            <- logger.error(s"pureharm — logger test $randomNumber — error")
        _ <- withClue("we fork and forget sending logs to amazon, so hence the wait") {
          Timer[IO].sleep(2.seconds)
        }
      } yield ()

    }

    testIO.unsafeRunSync()
  }
}
