package busymachines.pureharm.aws.s3

import busymachines.pureharm.anomaly._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
final case class InvalidAmazonS3RegionIA(r: String) extends InvalidInputAnomaly(s"Invalid AmazonS3 region: $r") {
  override val id: AnomalyID = S3AnomalyIDs.InvalidAmazonS3RegionID
}

final case class InvalidPathIA(p: String, cause: Throwable)
    extends InvalidInputAnomaly(
      message  = s"Invalid path: '$p'. Cause: ${cause.getLocalizedMessage}",
      causedBy = Option(cause),
    ) {
  override val id: AnomalyID = S3AnomalyIDs.InvalidFilePathID
}

object S3AnomalyIDs {
  case object InvalidAmazonS3RegionID extends AnomalyID { override val name: String = "H_AWS_S3_001" }
  case object InvalidFilePathID       extends AnomalyID { override val name: String = "H_NIO_001" }
}
