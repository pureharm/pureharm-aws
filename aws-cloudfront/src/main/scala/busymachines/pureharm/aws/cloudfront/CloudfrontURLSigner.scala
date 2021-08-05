/*
 * Copyright 2019 BusyMachines
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

package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.s3._

/*
 * @author Lorand Szakacs, https://github.com/lorandszakacs
 * @since 08 Jul 2019
 */
trait CloudfrontURLSigner[F[_]] {
  def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL]
}

object CloudfrontURLSigner {
  import java.security.PrivateKey

  def apply[F[_]: Sync](config: CloudfrontConfig): Resource[F, CloudfrontURLSigner[F]] = {
    val privateKeyF: F[PrivateKey] = config match {
      case kf: CloudfrontConfig.WithKeyFile    =>
        impl.loadPrivateKeyFromPath[F](kf.privateKeyFilePath)
      case pk: CloudfrontConfig.WithPrivateKey =>
        impl.loadPrivateKeyFromMemory[F](pk.privateKeyFormat, pk.privateKey)
    }

    for {
      pk <- Resource.eval(privateKeyF) // Resource.fromDestroyable(privateKey): TODO: upon closing this fails :(
    } yield new impl.CloudfrontURLSignerImpl(pk, config)
  }

  private object impl {
    import com.amazonaws.services.cloudfront.CloudFrontUrlSigner
    import com.amazonaws.services.cloudfront.util.SignerUtils

    def signS3KeyCanned[F[_]: Sync](
      privateKey: PrivateKey,
      config:     CloudfrontConfig,
    )(s3key:      S3FileKey): F[CloudfrontSignedURL] =
      for {
        baseURL   <- impl.createBaseUrl(config.distributionDomain)(s3key).pure[F]
        expiresAt <- TimeUtil.computeExpirationDate[F](config.urlExpirationTime)
        signed    <- impl.signCanned[F](
          privateKey = privateKey,
          keyPairID  = config.keyPairID,
          baseURL    = baseURL,
          expiresAt  = expiresAt,
        )
      } yield CloudfrontSignedURL(signed)

    /** We have to load private key from fuck-all the way where AWS stores it :/ it necessarily needs to be in .DER
      * format. See
      * [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      */
    def loadPrivateKeyFromMemory[F[_]](
      format: CloudfrontPrivateKey.Format,
      kp:     CloudfrontPrivateKey,
    )(implicit
      F:      Sync[F]
    ): F[PrivateKey] =
      F.blocking {
        import java.io.ByteArrayInputStream
        val bytes = kp.utf8Bytes
        format match {
          case CloudfrontPrivateKey.PEM =>
            com.amazonaws.auth.PEM.readPrivateKey(new ByteArrayInputStream(bytes)): PrivateKey
        }
      }.adaptError { case e => CloudFrontKeyReadingCatastrophe(e) }

    /** We have to load private key from fuck-all the way where AWS stores it :/ it necessarily needs to be in .DER
      * format. See
      * [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      */
    def loadPrivateKeyFromPath[F[_]](
      kp: CloudfrontPrivateKeyFilePath
    )(implicit
      F: Sync[F]
    ): F[PrivateKey] =
      F.blocking(SignerUtils.loadPrivateKey(kp.toFile)).adaptError { case e => CloudFrontKeyReadingCatastrophe(e) }

    /** See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      * Creates that url from the distributionDomain + S3FileKey
      *
      * Which then has to be signed.
      */
    def createBaseUrl(distributionDomain: CloudfrontDistributionDomain)(s3key: S3FileKey): String =
      s"https://${distributionDomain.show}/${s3key.show}"

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
      }.adaptError { case e => CloudFrontURLSigningCatastrophe(e) }

    final class CloudfrontURLSignerImpl[F[_]](
      private val privateKey: PrivateKey,
      private val config:     CloudfrontConfig,
    )(implicit
      private val F:          Sync[F]
    ) extends CloudfrontURLSigner[F] {

      override def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL] =
        CloudfrontURLSigner.impl.signS3KeyCanned[F](privateKey, config)(s3key)
    }
  }

}
