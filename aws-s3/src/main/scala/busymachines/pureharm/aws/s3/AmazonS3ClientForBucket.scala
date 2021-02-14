/** Copyright (c) 2017-2019 BusyMachines
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

import busymachines.pureharm.effects.ConcurrentEffect
import software.amazon.awssdk.services.s3.S3AsyncClient

/** Convenience trait.
  * Same as [[AmazonS3Client]] but uses the same bucket
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  */
trait AmazonS3ClientForBucket[F[_]] {

  def bucket: S3Bucket

  /** @see [[AmazonS3Client.initBucket]]
    */
  def initBucket: F[Unit]

  def put(key: S3FileKey, content: S3BinaryContent): F[Unit]

  def get(key: S3FileKey): F[S3BinaryContent]

  def putStream(key: S3FileKey, content: S3BinaryStream[F])(implicit F: ConcurrentEffect[F]): F[Unit]

  def getStream(key: S3FileKey, chunkSize: Int = 1024 * 512): S3BinaryStream[F]

  def delete(key: S3FileKey): F[Unit]

  def list(prefix: S3Path): F[List[S3FileKey]]

  def exists(key: S3FileKey): F[Boolean]

  def copy(fromKey: S3FileKey, toKey: S3FileKey): F[Unit]

  def copy(fromKey: S3FileKey, toBucket: S3Bucket, toKey: S3FileKey): F[Unit]

  def downloadURL(key: S3FileKey): F[S3DownloadURL]

  def s3Client: AmazonS3Client[F]

  def unsafeJavaClient: S3AsyncClient

  def config: S3Config
}
