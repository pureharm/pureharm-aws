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

sealed trait CloudfrontConfig {
  def distributionDomain: CloudfrontDistributionDomain
  def keyPairID:          CloudfrontKeyPairID
  def urlExpirationTime:  CloudfrontURLExpiration
}

object CloudfrontConfig {

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
}
