package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import scala.concurrent.duration._

import org.scalatest.funsuite.AnyFunSuite

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Jul 2019
  *
  */
class CloudfrontConfigLoaderTest extends AnyFunSuite {

  test("load config values") {
    val configIO = CloudfrontConfig.fromNamespace[IO]("test-config.pureharm.aws.cloudfront")
    assert(
      configIO.unsafeRunSync() === CloudfrontConfig(
        distributionDomain = CloudfrontDistributionDomain("test.cloudfront.net"),
        privateKeyFilePath = CloudfrontPrivateKeyFilePath("test-key"),
        keyPairID          = CloudfrontKeyPairID("test-key-pair-id"),
        urlExpirationTime  = CloudfrontURLExpiration(7.days),
      )
    )
  }
}
