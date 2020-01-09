package busymachines.pureharm.aws.sns

import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 27 Nov 2019
  *
  */
case class SNSMobilePushConfig(
  accessKeyID:             SNSAccessKeyID,
  secretAccessKey:         SNSSecretAccessKey,
  applicationARN:             SNSPlatformApplicationARN,
  arnEndpointCreationRetries: Int,
  arnEndpointCreationTimeout: FiniteDuration,
)

import busymachines.pureharm.effects._
import busymachines.pureharm.config._

object SNSMobilePushConfig extends ConfigLoader[SNSMobilePushConfig] {
  import busymachines.pureharm.config.implicits._

  implicit override val configReader: ConfigReader[SNSMobilePushConfig] = semiauto.deriveReader[SNSMobilePushConfig]

  override def default[F[_]: Sync]: F[SNSMobilePushConfig] = this.load("pureharm.aws.sns")
}
