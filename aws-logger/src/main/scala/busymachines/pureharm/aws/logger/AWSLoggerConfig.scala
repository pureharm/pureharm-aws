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

import busymachines.pureharm.config._
import busymachines.pureharm.effects._

import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Apr 2019
  *        ---
  * @param logTimeout
  *   if a log cannot be sent within this time to AWS, then timeout.
  */
final case class AWSLoggerConfig(
  logsToCloudEnabled:  Boolean,
  region:              String,
  logTimeout:          FiniteDuration,
  logsAccessKeyID:     String,
  logsSecretAccessKey: String,
  logsGroupName:       String,
  logsStreamName:      String,
)

object AWSLoggerConfig extends ConfigLoader[AWSLoggerConfig] {
  import busymachines.pureharm.config.implicits._

  implicit override val configReader: ConfigReader[AWSLoggerConfig] = semiauto.deriveReader[AWSLoggerConfig]
  override def default[F[_]: Sync]: F[AWSLoggerConfig] = this.load("pureharm.aws.logger")
}
