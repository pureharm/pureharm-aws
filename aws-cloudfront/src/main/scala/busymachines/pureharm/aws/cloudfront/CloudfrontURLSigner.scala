package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.aws.s3._

/**
  * Either use this for a stable configuration,
  * or [[CloudfrontURLSigner.signS3KeyCanned]] method on companion
  * object to be used in various configurations
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  *
  */
trait CloudfrontURLSigner[F[_]] {
  def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL]
}

object CloudfrontURLSigner {

  def apply[F[_]: Sync: BlockingShifter](config: CloudfrontConfig): CloudfrontURLSigner[F] =
    new impl.CloudfrontURLSignerImpl[F](config)

  import busymachines.pureharm.effects.implicits._

  def signS3KeyCanned[F[_]: Sync: BlockingShifter](
    config: CloudfrontConfig,
  )(s3key:  S3FileKey): F[CloudfrontSignedURL] = {
    for {
      baseURL    <- impl.createBaseUrl(config.distributionDomain)(s3key).pure[F]
      privateKey <- impl.loadPrivateKey[F](config.privateKeyFilePath)
      expiresAt  <- impl.computeExpirationDate[F](config.urlExpirationTime)
      signed <- impl.signCanned[F](
        privateKey = privateKey,
        keyPairID  = config.keyPairID,
        baseURL    = baseURL,
        expiresAt  = expiresAt,
      )
    } yield CloudfrontSignedURL(signed)
  }

  private[cloudfront] object impl {
    import java.security.PrivateKey
    import com.amazonaws.services.cloudfront.CloudFrontUrlSigner
    import com.amazonaws.services.cloudfront.util.SignerUtils

    /**
      * We have to load private key from fuck-all the way where AWS stores it :/
      * it necessarily needs to be in .DER format.
      * See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      */
    def loadPrivateKey[F[_]](
      key: CloudfrontPrivateKeyFilePath,
    )(
      implicit
      F:       Sync[F],
      blocker: BlockingShifter[F],
    ): F[PrivateKey] = {
      //using this instead of blocker.delay, to ensure that the error is adapted
      //on the same thread loading the private key happens
      blocker.blockOn {
        F.delay(SignerUtils.loadPrivateKey(key)).adaptError { case e => CloudFrontKeyReadingCatastrophe(e) }
      }
    }

    /**
      * See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      * Creates that url from the distributionDomain + S3FileKey
      *
      * Which then has to be signed.
      */
    def createBaseUrl(distributionDomain: CloudfrontDistributionDomain)(s3key: S3FileKey): String = {
      s"https://${distributionDomain.show}/${s3key.show}"
    }

    def computeExpirationDate[F[_]](expireIn: CloudfrontURLExpiration)(implicit F: Sync[F]): F[java.util.Date] = {
      F.delay {
        val deadline = expireIn.fromNow
        val date     = new java.util.Date()
        date.setTime(deadline.time.toMillis)
        date
      }
    }

    def signCanned[F[_]](
      privateKey: PrivateKey,
      keyPairID:  CloudfrontKeyPairID,
      baseURL:    String,
      expiresAt:  java.util.Date,
    )(implicit F: Sync[F]): F[String] =
      F.delay {
          CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
            baseURL,
            keyPairID,
            privateKey,
            expiresAt,
          )
        }
        .adaptError { case e => CloudFrontURLSigningCatastrophe(e) }

    final class CloudfrontURLSignerImpl[F[_]](
      private val config: CloudfrontConfig,
    )(
      implicit private val F:       Sync[F],
      implicit private val blocker: BlockingShifter[F],
    ) extends CloudfrontURLSigner[F] {

      override def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL] =
        CloudfrontURLSigner.signS3KeyCanned[F](config)(s3key)
    }
  }

}
