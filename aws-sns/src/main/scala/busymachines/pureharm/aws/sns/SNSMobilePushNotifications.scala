package busymachines.pureharm.aws.sns

import busymachines.pureharm.effects._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jan 2020
  *
  */
trait SNSMobilePushNotifications[F[_], K] {

  /**
    * You can register multiple device tokens to a single target
    *
    */
  def registerDevice(target: K, deviceToken: SNSDeviceToken): F[Unit]

  def pushMessage[Message: SNSMessageEncoder](target: K, message: Message): F[Unit]

}

object SNSMobilePushNotifications {

  import software.amazon.awssdk.auth.credentials._
  import software.amazon.awssdk.services.sns._
  import busymachines.pureharm.aws.sns.internals._

  def resource[F[_]: Concurrent: Timer: BlockingShifter, K](
    config: SNSMobilePushConfig,
  ): Resource[F, SNSMobilePushNotifications[F, K]] = {
    for {
      jSNSClient <- Resource.liftF {
        Sync[F].delay {
          SnsClient
            .builder()
            .credentialsProvider(
              StaticCredentialsProvider
                .create(AwsBasicCredentials.create(config.accessKeyID, config.secretAccessKey)),
            )
            .build()
        }
      }

      ops = new SNSMobilePushNotificationsOps[F](jSNSClient)
      impl <- SNSMobilePushNotificationsImpl.create[F, K](config, ops)
    } yield impl
  }

}
