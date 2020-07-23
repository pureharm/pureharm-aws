package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.s3._

/**
  * Either use this for a stable configuration,
  * or [[CloudfrontURLSigner.signS3KeyCanned]] method on companion
  * object to be used in various configurations
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
trait CloudfrontURLSigner[F[_]] {
  def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL]
}

object CloudfrontURLSigner {

  def apply[F[_]: Sync: BlockingShifter](config: CloudfrontConfig): Resource[F, CloudfrontURLSigner[F]] =
    config match {
      case kf: CloudfrontConfig.WithKeyFile    => new impl.CloudfrontURLSignerImpl[F](kf).pure[Resource[F, *]]
      case pk: CloudfrontConfig.WithPrivateKey =>
        for {
          (keyPath, _) <- Resource.make[F, (CloudfrontPrivateKeyFilePath, java.nio.file.Path)](
            acquire = init.writePrivateKeyToTempFile[F](pk.privateKey, pk.privateKeyFormat)
          )(
            release = {
              case (kp, td) => init.deletePrivateKeyTempFile[F](td, kp)
            }
          )
          newConfig = CloudfrontConfig.WithKeyFile(
            distributionDomain = pk.distributionDomain,
            privateKeyFilePath = keyPath,
            keyPairID          = pk.keyPairID,
            urlExpirationTime  = pk.urlExpirationTime,
          )
        } yield new impl.CloudfrontURLSignerImpl[F](newConfig)
    }

  def signS3KeyCanned[F[_]: Sync: BlockingShifter](
    config: CloudfrontConfig.WithKeyFile
  )(s3key:  S3FileKey): F[CloudfrontSignedURL] =
    for {
      baseURL    <- impl.createBaseUrl(config.distributionDomain)(s3key).pure[F]
      privateKey <- impl.loadPrivateKey[F](config.privateKeyFilePath)
      expiresAt  <- TimeUtil.computeExpirationDate[F](config.urlExpirationTime)
      signed     <- impl.signCanned[F](
        privateKey = privateKey,
        keyPairID  = config.keyPairID,
        baseURL    = baseURL,
        expiresAt  = expiresAt,
      )
    } yield CloudfrontSignedURL(signed)

  private object init {
    import java.nio.file._
    import java.nio.file.attribute._

    /**
      * Basically the way this works is that it:
      *  - create a $folderName with a random name under /tmp
      *    this folder has permissions 600 (it has to be lower than 644).
      *    we use a random name in case of server crashes or something,
      *    so we don't have conflicts.
      *
      *  - create a /tmp/$folderName/temp_private_key.${pem or der)
      *    to store the decoded `privateKey` with permissions 600
      *
      *  This permission scheme is how you _have_ to store private keys :)
      *
      * @param privateKey
      *   The private key to be stored in a file
      *  @param privateKeyFormat
      *    one of .pem or .der
      * @return
      *   The full file path to be read w/ the cloudfront java SDK
      */
    def writePrivateKeyToTempFile[F[_]](
      privateKey:       CloudfrontPrivateKey,
      privateKeyFormat: CloudfrontPrivateKey.Format,
    )(implicit
      F:                Sync[F],
      bs:               BlockingShifter[F],
    ): F[(CloudfrontPrivateKeyFilePath, Path)] = {

      val tempDirRoot = Path.of("/tmp")
      val perms600: java.util.Set[PosixFilePermission] = {
        val temp = new java.util.HashSet[PosixFilePermission]()
        temp.add(PosixFilePermission.OWNER_READ);
        temp.add(PosixFilePermission.OWNER_WRITE);
        temp
      }
      val attr        = PosixFilePermissions.asFileAttribute(perms600)
      val write       = for {
        tempDirPath <- F.delay(Files.createTempDirectory(tempDirRoot, "cloudfront_key", attr)) //TODO: adapt errors
        tempKeyFilePath = Path.of(tempDirRoot.toString, s"temp_private_key${privateKeyFormat.toString}")
        afterWrite <- F.delay(Files.createTempFile(tempKeyFilePath, null, null, attr))
        finalPath  <- F.delay[Path](
          Files.write(afterWrite, privateKey.utf8Bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        )
      } yield (CloudfrontPrivateKeyFilePath(finalPath.toAbsolutePath.toString), tempDirPath)

      bs.blockOn(write)
    }

    def deletePrivateKeyTempFile[F[_]: Sync: BlockingShifter](
      tempDirPath: Path,
      kfp:         CloudfrontPrivateKeyFilePath,
    ): F[Unit] =
      BlockingShifter[F].blockOn {
        Sync[F].delay(Files.delete(Path.of(kfp))) >> Sync[F].delay(Files.delete(tempDirPath))
      }

  }

  private object impl {
    import java.security.PrivateKey
    import com.amazonaws.services.cloudfront.CloudFrontUrlSigner
    import com.amazonaws.services.cloudfront.util.SignerUtils

    /**
      * We have to load private key from fuck-all the way where AWS stores it :/
      * it necessarily needs to be in .DER format.
      * See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
      */
    def loadPrivateKey[F[_]](
      key:     CloudfrontPrivateKeyFilePath
    )(implicit
      F:       Sync[F],
      blocker: BlockingShifter[F],
    ): F[PrivateKey] =
      //using this instead of blocker.delay, to ensure that the error is adapted
      //on the same thread loading the private key happens
      blocker.blockOn {
        F.delay(SignerUtils.loadPrivateKey(key)).adaptError { case e => CloudFrontKeyReadingCatastrophe(e) }
      }

    /**
      * See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html]]
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
      private val config:           CloudfrontConfig.WithKeyFile
    )(
      implicit private val F:       Sync[F],
      implicit private val blocker: BlockingShifter[F],
    ) extends CloudfrontURLSigner[F] {

      override def signS3KeyCanned(s3key: S3FileKey): F[CloudfrontSignedURL] =
        CloudfrontURLSigner.signS3KeyCanned[F](config)(s3key)
    }
  }

}
