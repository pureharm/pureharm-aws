package busymachines.pureharm.aws.logger

import busymachines.pureharm.effects._
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 14 May 2019
  *
  */
final class LoadAWSConfigTest extends AnyFunSuite with Matchers {

  test("... read config from local test reference.conf") {
    noException shouldBe thrownBy {
      AWSLoggerConfig.default[IO].unsafeRunSync()
    }
  }
}
