package busymachines.pureharm.aws.sns.internals

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.sns._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Nov 2019
  *
  */
private[sns] class SNSMobilePushNotificationsImpl[F[_], K](
  private val snsConfig: SNSMobilePushConfig,
)(
  private val cache: NonExpiringCache[F, (K, SNSDeviceToken), SNSEndpointARN],
  private val ops:   SNSMobilePushNotificationsOps[F],
)(
  implicit
  private val F:     Sync[F],
  private val timer: Timer[F],
) extends SNSMobilePushNotifications[F, K] {

//  private[sns] def pushToDevice[Message: SNSMessageEncoder](deviceToken: SNSDeviceToken, message: Message): F[Unit] =
//    for {
//      arn <- findOrCreateEndpointARNForDeviceToken(deviceToken)
//      _   <- ops.pushMessage(arn, message)
//    } yield ()
//

//  private def findOrCreateEndpointARNForDeviceToken(deviceToken: SNSDeviceToken): F[SNSEndpointARN] =
//    for {
//      cachedARNEndpoint <- localCache.getOrPutIfAbsent(
//        deviceToken,
//        ops
//          .createEndpointARNForDeviceToken(snsConfig.applicationARN, deviceToken)
//          .reattempt(
//            snsConfig.arnEndpointCreationRetries,
//            snsConfig.arnEndpointCreationTimeout,
//          ),
//      )
//      status <- ops.healthcheckSNSEndpoint(deviceToken, cachedARNEndpoint)
//      verifiedARNEndpoint <- status match {
//        case SNSPlatformEndpointHealthcheck.Exists => F.pure(cachedARNEndpoint)
//        case SNSPlatformEndpointHealthcheck.Disabled | SNSPlatformEndpointHealthcheck.NotFound =>
//          localCache.replace(
//            deviceToken,
//            ops
//              .createEndpointARNForDeviceToken(snsConfig.applicationARN, deviceToken)
//              .reattempt(
//                snsConfig.arnEndpointCreationRetries,
//                snsConfig.arnEndpointCreationTimeout,
//              ),
//          )
//      }
//    } yield verifiedARNEndpoint

  private def findOrCreateEndpointARNForDeviceToken(target: K, deviceToken: SNSDeviceToken): F[SNSEndpointARN] =
    for {
      cachedARNEndpoint <- cache.getOrPutIfAbsent(
        target -> deviceToken,
        ops
          .createEndpointARNForDeviceToken(snsConfig.applicationARN, deviceToken)
          .reattempt(
            snsConfig.arnEndpointCreationRetries,
            snsConfig.arnEndpointCreationTimeout,
          ),
      )
      status <- ops.healthcheckSNSEndpoint(deviceToken, cachedARNEndpoint)
      verifiedARNEndpoint <- status match {
        case SNSPlatformEndpointHealthcheck.Exists => F.pure(cachedARNEndpoint)
        case SNSPlatformEndpointHealthcheck.Disabled | SNSPlatformEndpointHealthcheck.NotFound =>
          cache.replace(
            target -> deviceToken,
            ops
              .createEndpointARNForDeviceToken(snsConfig.applicationARN, deviceToken)
              .reattempt(
                snsConfig.arnEndpointCreationRetries,
                snsConfig.arnEndpointCreationTimeout,
              ),
          )
      }
    } yield verifiedARNEndpoint

  /**
    * You can register multiple device tokens to a single key
    *
    */
  override def registerDevice(target: K, deviceToken: SNSDeviceToken): F[Unit] = {
    findOrCreateEndpointARNForDeviceToken(target, deviceToken).void
  }

  override def pushMessage[Message: SNSMessageEncoder](target: K, message: Message): F[Unit] =
    for {
      deviceTokens <- cache.filter(kv => kv._1._1 == target).map(_.values.toList)
      _            <- deviceTokens.traverse(arn => ops.pushMessage(arn, message))
    } yield ()
}

object SNSMobilePushNotificationsImpl {

  def create[F[_], K](snsConfig: SNSMobilePushConfig, ops: SNSMobilePushNotificationsOps[F])(
    implicit
    F:     Concurrent[F],
    timer: Timer[F],
  ): Resource[F, SNSMobilePushNotificationsImpl[F, K]] = Resource.liftF {
    for {
      cache <- NonExpiringCache[F, (K, SNSDeviceToken), SNSEndpointARN]
    } yield new SNSMobilePushNotificationsImpl(snsConfig)(cache, ops)
  }

}
