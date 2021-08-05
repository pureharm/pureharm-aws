/** Copyright (c) 2017-2019 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
package busymachines.pureharm.aws.logger

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

import org.typelevel.log4cats.slf4j.Slf4jLogger

import busymachines.pureharm.testkit._
import scala.concurrent.duration._

/** test expects proper live amazon config
  *
  * Before running this ensure that you actually have the proper local environment variables. See the
  * ``pureharm-aws/aws-logger/src/test/resources/reference.conf`` for the environment variables that are used by this
  * test.
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04
  *   Sep 2019
  */
final class AWSLoggerLiveTest extends PureharmTest {

  implicit override val testLogger: TestLogger = TestLogger(Slf4jLogger.getLogger[IO])

  private val localLogger = Slf4jLogger.getLogger[IO]

  private val resource = ResourceFixture[AWSLogging[IO]] { (_: TestOptions) =>
    for {
      //AWSLoggerConfig.fromNamespaceR[IO]("test-live.pureharm.aws.logger")
      config  <- (??? : Resource[IO, AWSLoggerConfig])
      logFact <- AWSLogging.resource[IO](config)
      _       <-
        runtime.contextShift.shift
          .to[Resource[IO, *]] //shifting so that first parts of test are not run on scalatest-threads
    } yield logFact
  }

  resource.test("... should send logs to AWS cloud") { awsLogging =>
    for {
      randomNumber <- IO(Math.abs(scala.util.Random.nextLong()))
      _            <- localLogger.info(s"LOCAL — look manually for number '$randomNumber' in aws cloudwatch logs")
      logger       <- awsLogging.getLogger(localLogger).pure[IO]
      _            <- logger.trace(s"pureharm — logger test $randomNumber — trace")
      _            <- logger.debug(s"pureharm — logger test $randomNumber — debug")
      _            <- logger.info(s"pureharm — logger test $randomNumber — info")
      _            <- logger.warn(s"pureharm — logger test $randomNumber — warn")
      _            <- logger.error(s"pureharm — logger test $randomNumber — error")
      _            <- Timer[IO].sleep(5.seconds) // we wait to ensure that the logs are sent
    } yield ()
  }
}
