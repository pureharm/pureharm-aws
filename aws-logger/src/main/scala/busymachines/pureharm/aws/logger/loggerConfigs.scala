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

package busymachines.pureharm.aws.logger

import busymachines.pureharm.aws.core._

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09
  *   Apr 2019
  * ---
  */
sealed trait AWSLoggerConfig extends Product with Serializable {
  def enabled:    AWSLoggingEnabled
  def cloudwatch: Option[CloudWatchLoggerConfig]
}

final case class EnabledAWSLoggerConfig(
  someCloudwatch: CloudWatchLoggerConfig
) extends AWSLoggerConfig {
  override val cloudwatch: Option[CloudWatchLoggerConfig] = Option(someCloudwatch)
  override val enabled:    AWSLoggingEnabled              = AWSLoggingEnabled.True
}

case object DisabledAWSLoggerConfig extends AWSLoggerConfig {
  override val cloudwatch: Option[CloudWatchLoggerConfig] = Option.empty
  override val enabled:    AWSLoggingEnabled              = AWSLoggingEnabled.False
}

/** @param timeout
  *   if a log cannot be sent within this time to AWS, then timeout.
  *
  * @param region
  *   the amazon region of the CloudWatch you are configuring
  */
final case class CloudWatchLoggerConfig(
  timeout:         CloudWatchTimeoutDuration,
  region:          AmazonRegion,
  accessKeyID:     CloudWatchAccessKeyID,
  secretAccessKey: CloudWatchSecretAccessKey,
  groupName:       CloudWatchGroupName,
  streamName:      CloudWatchStreamName,
)
