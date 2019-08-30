package busymachines.pureharm.aws.s3

import busymachines.pureharm.aws.core._
import busymachines.pureharm.effects._
/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */

final case class S3Config(
  s3AccessKeyId:         S3AccessKeyID,
  secretAccessKey:       S3SecretAccessKey,
  region:                AmazonRegion,
  bucket:                S3Bucket,
  apiCallAttemptTimeout: S3ApiCallAttemptTimeout,
  apiCallTimeout:        S3ApiCallTimeout,
  headers:               List[AmazonRequestHeader] = List.empty,
)

import busymachines.pureharm.config._
import busymachines.pureharm.config.implicits._

object S3Config extends ConfigLoader[S3Config] {
  implicit val AmazonRequestHeaderReader: ConfigReader[AmazonRequestHeader] = semiauto.deriveReader[AmazonRequestHeader]
  implicit override val configReader:     ConfigReader[S3Config]            = semiauto.deriveReader[S3Config]
  override def default[F[_]: Sync]: F[S3Config] = this.load("pureharm.aws.s3")
}
