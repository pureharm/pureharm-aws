package busymachines.pureharm.aws

import busymachines.pureharm.phantom._

import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
package object s3 {

  type S3FileKey = S3FileKey.Type

  object S3BinaryContent extends PhantomType[Array[Byte]]
  type S3BinaryContent = S3BinaryContent.Type

  object S3Bucket extends PhantomType[String]
  type S3Bucket = S3Bucket.Type

  object S3AccessKeyID extends PhantomType[String]
  type S3AccessKeyID = S3AccessKeyID.Type

  object S3SecretAccessKey extends PhantomType[String]
  type S3SecretAccessKey = S3SecretAccessKey.Type

  object S3ApiCallAttemptTimeout extends PhantomType[FiniteDuration]
  type S3ApiCallAttemptTimeout = S3ApiCallAttemptTimeout.Type

  object S3ApiCallTimeout extends PhantomType[FiniteDuration]
  type S3ApiCallTimeout = S3ApiCallTimeout.Type

  object S3DownloadURL extends PhantomType[String]
  type S3DownloadURL = S3DownloadURL.Type
}
