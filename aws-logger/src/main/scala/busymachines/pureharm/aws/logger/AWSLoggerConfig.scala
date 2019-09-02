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
