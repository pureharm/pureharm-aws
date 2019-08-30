package busymachines.pureharm.aws.core

import busymachines.pureharm.anomaly._
/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
final case class InvalidAmazonS3RegionIA(r: String) extends InvalidInputAnomaly(s"Invalid AmazonS3 region: $r") {
  override val id: AnomalyID = InvalidAmazonS3RegionID
}
case object InvalidAmazonS3RegionID extends AnomalyID { override val name: String = "H_AWS_S3_001" }
