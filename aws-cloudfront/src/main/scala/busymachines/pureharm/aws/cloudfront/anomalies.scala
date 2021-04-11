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

final case class InvalidCloudfrontPEMKey()
  extends InvalidInputAnomaly(
    message = s"""|Invalid PEM key. It did not include with 'BEGIN RSA PRIVATE KEY'. 
                  |Assuming it's not a correct private key.
                  |
                  |Keep in mind that you take the .pem cloudfront keyfile, and base64 encode it.
                  |And pass that _string_ along as a parameter.
                  |
                  |e.g.
                  |
                  |$$ base64 ~/some_path/my_cloudfront_private_key.pem
                  |""".stripMargin
  ) {
  override val id: AnomalyID = CloudfrontAnomalyIDs.CloudFrontInvalidPrivateKeyID
}

object CloudfrontAnomalyIDs {
  case object CloudFrontKeyReadingCatastropheID extends AnomalyID { override val name: String = "PH_AWS_CF_001" }
  case object CloudFrontURLSigningCatastropheID extends AnomalyID { override val name: String = "PH_AWS_CF_002" }
  case object CloudFrontInvalidPrivateKeyID     extends AnomalyID { override val name: String = "PH_AWS_CF_003" }
}
