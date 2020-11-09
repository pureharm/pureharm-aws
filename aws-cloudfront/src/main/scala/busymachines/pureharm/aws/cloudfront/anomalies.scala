package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.anomaly._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Jul 2019
  */
final case class CloudFrontKeyReadingCatastrophe(cause: Throwable)
  extends InconsistentStateCatastrophe(
    message  = s"Failed to read cloudfront private key because; $cause",
    causedBy = Option(cause),
  ) {
  override val id: AnomalyID = CloudfrontAnomalyIDs.CloudFrontKeyReadingCatastropheID
}

final case class CloudFrontURLSigningCatastrophe(cause: Throwable)
  extends InconsistentStateCatastrophe(
    message  = s"Failed to sign cloudfront URL because; $cause",
    causedBy = Option(cause),
  ) {
  override val id: AnomalyID = CloudfrontAnomalyIDs.CloudFrontURLSigningCatastropheID
}

object CloudfrontAnomalyIDs {
  case object CloudFrontKeyReadingCatastropheID extends AnomalyID { override val name: String = "PH_AWS_CF_001" }
  case object CloudFrontURLSigningCatastropheID extends AnomalyID { override val name: String = "PH_AWS_CF_002" }
}
