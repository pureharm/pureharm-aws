/*
 * Copyright 2019 BusyMachines
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

package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.testkit._
import org.typelevel.log4cats.slf4j._

import scala.concurrent.duration._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Jul 2019
  */
final class CloudfrontConfigLoaderTest extends PureharmTest {

  implicit override val testLogger: TestLogger = TestLogger(Slf4jLogger.getLogger[IO])

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
