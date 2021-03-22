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
