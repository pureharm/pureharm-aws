package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.testkit.PureharmTest

import scala.concurrent.duration._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Jul 2019
  */
final class CloudfrontConfigLoaderTest extends PureharmTest {

  test("load config values — CloudfrontConfig.WithKeyFile") {
    for {
      config <- CloudfrontConfig.fromNamespace[IO]("test-config.pureharm.aws.cloudfront-with-key-path")
    } yield assert(
      config === CloudfrontConfig.WithKeyFile(
        distributionDomain = CloudfrontDistributionDomain("test.cloudfront.net"),
        privateKeyFilePath = CloudfrontPrivateKeyFilePath(java.nio.file.Path.of("test-key")),
        keyPairID          = CloudfrontKeyPairID("test-key-pair-id"),
        urlExpirationTime  = CloudfrontURLExpiration(7.days),
      )
    )
  }

  test("load config values — CloudfrontConfig.WithPrivateKey — PEM") {
    for {
      config <- CloudfrontConfig.fromNamespace[IO]("test-config.pureharm.aws.cloudfront-with-key-value-pem")
    } yield assert(
      config === CloudfrontConfig.WithPrivateKey(
        distributionDomain = CloudfrontDistributionDomain("test.cloudfront.net"),
        keyPairID          = CloudfrontKeyPairID("test-key-pair-id"),
        privateKey         = CloudfrontPrivateKey[Try]("BLABLABLA").get,
        privateKeyFormat   = CloudfrontPrivateKey.PEM,
        urlExpirationTime  = CloudfrontURLExpiration(7.days),
      )
    )
  }

  test("load config values — CloudfrontConfig.WithPrivateKey — DER") {
    for {
      config <- CloudfrontConfig.fromNamespace[IO]("test-config.pureharm.aws.cloudfront-with-key-value-der")
    } yield assert(
      config === CloudfrontConfig.WithPrivateKey(
        distributionDomain = CloudfrontDistributionDomain("test.cloudfront.net"),
        keyPairID          = CloudfrontKeyPairID("test-key-pair-id"),
        privateKey         = CloudfrontPrivateKey[Try]("BLABLABLA").get,
        privateKeyFormat   = CloudfrontPrivateKey.DER,
        urlExpirationTime  = CloudfrontURLExpiration(7.days),
      )
    )
  }
}
