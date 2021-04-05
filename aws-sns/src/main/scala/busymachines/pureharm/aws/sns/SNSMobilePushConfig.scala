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

package busymachines.pureharm.aws.sns

import busymachines.pureharm.aws.core.AmazonRegion
import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 27 Nov 2019
  */
case class SNSMobilePushConfig(
  accessKeyID:                SNSAccessKeyID,
  secretAccessKey:            SNSSecretAccessKey,
  region:                     AmazonRegion,
  applicationARN:             SNSPlatformApplicationARN,
  arnEndpointCreationRetries: Int,
  arnEndpointCreationTimeout: FiniteDuration,
)

import busymachines.pureharm.config._

@scala.deprecated(
  "Companion object that depends on pureharm-config will be deprecated and removed. Use pureharm-config-ciris in userland to read the config instead.",
  "0.2.0",
)
object SNSMobilePushConfig extends ConfigLoader[SNSMobilePushConfig] {
  import busymachines.pureharm.config.implicits._

  implicit override val configReader: ConfigReader[SNSMobilePushConfig] = semiauto.deriveReader[SNSMobilePushConfig]
}
