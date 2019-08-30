package busymachines.pureharm.aws.s3

/**
  *
  * Convenience trait.
  * Same as [[AmazonS3Client]] but uses the same bucket
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
trait AmazonS3ClientForBucket[F[_]] {

  def bucket: S3Bucket

  def put(key: S3FileKey, content: S3BinaryContent): F[Unit]

  def get(key: S3FileKey): F[S3BinaryContent]

  def delete(key: S3FileKey): F[Unit]

  def downloadURL(key: S3FileKey): F[S3DownloadURL]
}
