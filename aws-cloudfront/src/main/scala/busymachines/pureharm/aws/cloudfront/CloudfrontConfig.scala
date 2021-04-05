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
import busymachines.pureharm.config._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
sealed trait CloudfrontConfig {
  def distributionDomain: CloudfrontDistributionDomain
  def keyPairID:          CloudfrontKeyPairID
  def urlExpirationTime:  CloudfrontURLExpiration
}

@scala.deprecated(
  "Companion object that depends on pureharm-config will be deprecated and removed. Use pureharm-config-ciris in userland to read the config instead.",
  "0.2.0",
)
object CloudfrontConfig extends ConfigLoader[CloudfrontConfig] {
  import busymachines.pureharm.config.implicits._
  import busymachines.pureharm.effects.implicits._
  import pureconfig.error.CannotConvert

  final case class WithKeyFile(
    override val distributionDomain: CloudfrontDistributionDomain,
    override val keyPairID:          CloudfrontKeyPairID,
    override val urlExpirationTime:  CloudfrontURLExpiration,
    privateKeyFilePath:              CloudfrontPrivateKeyFilePath,
  ) extends CloudfrontConfig

  final case class WithPrivateKey(
    override val distributionDomain: CloudfrontDistributionDomain,
    override val keyPairID:          CloudfrontKeyPairID,
    override val urlExpirationTime:  CloudfrontURLExpiration,
    privateKey:                      CloudfrontPrivateKey,
    privateKeyFormat:                CloudfrontPrivateKey.Format,
  ) extends CloudfrontConfig

  implicit protected val privateKeyReader: ConfigReader[CloudfrontPrivateKey] =
    ConfigReader[String].emap(s =>
      CloudfrontPrivateKey[Attempt](s).leftMap(an =>
        CannotConvert(
          value   = s"CloudfrontPrivateKeyValue — truncated(20): ${s.take(20)}",
          toType  = "CloudfrontPrivateKey",
          because = an.toString,
        )
      )
    )

  implicit protected val formatReader: ConfigReader[CloudfrontPrivateKey.Format]  =
    semiauto.deriveEnumerationReader(transformName = n => s".${n.toLowerCase}")

  implicit private val withKeyFileCFG: ConfigReader[CloudfrontConfig.WithKeyFile] =
    semiauto.deriveReader[CloudfrontConfig.WithKeyFile]

  implicit private val withPrivateKeyCFG: ConfigReader[CloudfrontConfig.WithPrivateKey] =
    semiauto.deriveReader[CloudfrontConfig.WithPrivateKey]

  implicit override val configReader: ConfigReader[CloudfrontConfig] =
    withPrivateKeyCFG.orElse(withKeyFileCFG)
}
