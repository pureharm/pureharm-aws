package busymachines.pureharm.aws.sns

/**
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 29 Nov 2019
  */
sealed trait SNSPlatformEndpointHealthcheck extends Product with Serializable

object SNSPlatformEndpointHealthcheck {
  /**
    * @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[sns] val TokenAttributeID   = "Token"
  /**
    * @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[sns] val EnabledAttributeID = "Enabled"

  //facepalm
  private[sns] val TrueString = "true"

  case object Exists   extends SNSPlatformEndpointHealthcheck
  case object NotFound extends SNSPlatformEndpointHealthcheck
  case object Disabled extends SNSPlatformEndpointHealthcheck
}
