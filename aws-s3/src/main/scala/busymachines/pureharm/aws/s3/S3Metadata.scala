package busymachines.pureharm.aws.s3

import software.amazon.awssdk.services.s3.model.HeadObjectResponse

/** @param contentLength
  * Neatly parsed and categorized metadata
  *
  * @param javaMetadata
  * All metadata returned by the Java SDK,
  * in time this will be phased out, as we
  * add more and more properly structured data
  * to this case class.
  */
final case class S3Metadata(
  contentLength: S3ContentLengthBytes,
  javaMetadata:  HeadObjectResponse,
)
