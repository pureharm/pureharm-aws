package busymachines.pureharm.aws.sns.internals

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 29 Nov 2019
  *
  */
sealed private[internals] trait SNSPlatformEndpointHealthcheck extends Product with Serializable

private[internals] object SNSPlatformEndpointHealthcheck {
  /**
    * @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[internals] val TokenAttributeID = "Token"
  /**
    * @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[internals] val EnabledAttributeID = "Enabled"

  //facepalm
  private[internals] val TrueString = "true"

  case object Exists   extends SNSPlatformEndpointHealthcheck
  case object NotFound extends SNSPlatformEndpointHealthcheck
  case object Disabled extends SNSPlatformEndpointHealthcheck
}
