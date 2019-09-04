/**
  * Copyright (c) 2017-2019 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
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

import busymachines.pureharm.aws.core._
import busymachines.pureharm.effects._
/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */

final case class S3Config(
  region:                AmazonRegion,
  accessKeyID:           S3AccessKeyID,
  secretAccessKey:       S3SecretAccessKey,
  bucket:                S3Bucket,
  apiCallAttemptTimeout: S3ApiCallAttemptTimeout,
  apiCallTimeout:        S3ApiCallTimeout,
  headers:               List[AmazonRequestHeader] = List.empty,
)

import busymachines.pureharm.config._

object S3Config extends ConfigLoader[S3Config] {
  import busymachines.pureharm.config.implicits._
  import busymachines.pureharm.aws.core.implicits._

  implicit val AmazonRequestHeaderReader: ConfigReader[AmazonRequestHeader] = semiauto.deriveReader[AmazonRequestHeader]
  implicit override val configReader:     ConfigReader[S3Config]            = semiauto.deriveReader[S3Config]
  override def default[F[_]: Sync]: F[S3Config] = this.load("pureharm.aws.s3")
}
