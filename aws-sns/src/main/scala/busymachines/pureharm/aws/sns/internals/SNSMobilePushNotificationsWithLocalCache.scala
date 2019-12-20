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
private[sns] class SNSMobilePushNotificationsWithLocalCache[F[_]](
  private val snsConfig: SNSMobilePushConfig,
)(
  private val localCache: SimpleCache[F, SNSDeviceToken, SNSEndpointARN],
  private val ops:        SNSMobilePushNotificationsOps[F],
)(
  implicit
  private val F:               Sync[F],
  private val timer:           Timer[F],
  private val blockingShifter: BlockingShifter[F],
) {

  private[sns] def pushToDevice[Message: SNSMessageEncoder](deviceToken: SNSDeviceToken, message: Message): F[Unit] =
    for {
      arn <- findOrCreateEndpointARNForDeviceToken(deviceToken)
      _   <- ops.pushMessage(arn, message)
    } yield ()

  private def findOrCreateEndpointARNForDeviceToken(deviceToken: SNSDeviceToken): F[SNSEndpointARN] =
    for {
      arnEndpoint <- localCache.getOrPutIfAbsent(
        deviceToken,
        ops
          .createEndpointARNForDeviceToken(snsConfig.applicationARN, deviceToken)
          .reattempt(
            snsConfig.arnEndpointCreationRetries,
            snsConfig.arnEndpointCreationTimeout,
          ),
      )
    } yield arnEndpoint

}
