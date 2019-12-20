package busymachines.pureharm.aws.sns

import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 27 Nov 2019
  *
  */
case class SNSMobilePushConfig(
  applicationARN:             SNSPlatformApplicationARN,
  arnEndpointCreationRetries: Int,
  arnEndpointCreationTimeout: FiniteDuration,
)
