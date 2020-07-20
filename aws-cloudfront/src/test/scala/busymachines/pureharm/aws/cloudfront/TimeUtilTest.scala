package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import scala.concurrent.duration._

import org.scalatest.funsuite.AnyFunSuite

/**
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 05 May 2020
  */
class TimeUtilTest extends AnyFunSuite {

  test("compute expiration date compared to 'now'") {

    val delta     = 2.seconds
    val expiresIn = 5.minutes

    val t = for {
      result <- TimeUtil.computeExpirationDate[IO](CloudfrontURLExpiration(expiresIn))
      now    <- IO(new java.util.Date())
      diff = Duration(Math.abs((now.getTime + expiresIn.toMillis) - result.getTime), MILLISECONDS)
    } yield assert(diff <= delta)

    t.unsafeRunSync()

  }
}
