/** Copyright (c) 2017-2019 BusyMachines
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

import busymachines.pureharm.aws.core._
import busymachines.pureharm.config._
import busymachines.pureharm.effects._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Apr 2019
  *        ---
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
  *  the amazon region of the CloudWatch you are configuring
  */
final case class CloudWatchLoggerConfig(
  timeout:         CloudWatchTimeoutDuration,
  region:          AmazonRegion,
  accessKeyID:     CloudWatchAccessKeyID,
  secretAccessKey: CloudWatchSecretAccessKey,
  groupName:       CloudWatchGroupName,
  streamName:      CloudWatchStreamName,
)

object CloudWatchLoggerConfig {
  import busymachines.pureharm.config.implicits._

  implicit val cloudWatchConfigReader: ConfigReader[CloudWatchLoggerConfig] =
    semiauto.deriveReader[CloudWatchLoggerConfig]
}

/** Config reading will behave the following way:
  * 1) enabled = true, then we attempt to read a well-formed cloudwatch subobject
  *
  * {{{
  * namespace {
  *    enabled = true
  *    cloudwatch {
  *      //properly defined
  *    }
  * }
  * }}}
  *
  * 2) enabled = false, then no attempt is made to read the cloudwatch object
  *
  * {{{
  * namespace {
  *   enabled = false
  *   //cloudwatch object can be entirely elided
  *   cloudwatch {
  *     //doesn't matter if well formed, will be ignored anyway
  *   }
  * }
  * }}}
  */
object AWSLoggerConfig extends ConfigLoader[AWSLoggerConfig] {
  import busymachines.pureharm.config.implicits._

  private object impl {

    final private case class AWSLoggerConfigEnabledStatus(
      enabled: Boolean
    )

    private object AWSLoggerConfigEnabledStatus {

      implicit val configReader: ConfigReader[AWSLoggerConfigEnabledStatus] =
        semiauto.deriveReader[AWSLoggerConfigEnabledStatus]
    }

    final private case class AWSLoggerConfigTemp(
      cloudwatch: CloudWatchLoggerConfig
    )

    private object AWSLoggerConfigTemp {

      implicit val configReader: ConfigReader[AWSLoggerConfigTemp] =
        semiauto.deriveReader[AWSLoggerConfigTemp]
    }

    implicit private[AWSLoggerConfig] val awsLoggerConfigReaderFallback: ConfigReader[AWSLoggerConfig] = for {
      es     <- AWSLoggerConfigEnabledStatus.configReader
      config <-
        if (es.enabled) {
          AWSLoggerConfigTemp.configReader.map(t => EnabledAWSLoggerConfig(t.cloudwatch): AWSLoggerConfig)
        }
        else {
          DisabledAWSLoggerConfigReader
        }
    } yield config

    private object DisabledAWSLoggerConfigReader extends ConfigReader[AWSLoggerConfig] {
      import pureconfig.ConfigCursor

      override def from(cur: ConfigCursor): ConfigReader.Result[AWSLoggerConfig] =
        Right(DisabledAWSLoggerConfig): ConfigReader.Result[AWSLoggerConfig]
    }
  }

  implicit override val configReader: ConfigReader[AWSLoggerConfig] = impl.awsLoggerConfigReaderFallback
  override def default[F[_]: Sync]: F[AWSLoggerConfig] = this.load("pureharm.aws.logger")
}
