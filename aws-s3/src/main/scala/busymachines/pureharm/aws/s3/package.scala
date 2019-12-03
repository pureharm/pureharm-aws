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

  type S3Path = S3Path.Type

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
