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

import busymachines.pureharm.effects._
/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 23 Jul 2019
  *
  */

trait S3FileKeyImplicits {
  @inline implicit def pureharmS3FileKeyOps(s3FileKey: S3FileKey): S3FileKeyOps = new S3FileKeyOps(s3FileKey)
}

final class S3FileKeyOps(val s3FileKey: S3FileKey) extends AnyVal {

  /**
    *
    * @param path
    *   Should not end or start in slash
    * @return
    *   given a key "x.foo" creates "$path/x.foo"
    */
  def prependPath(path: String): Attempt[S3FileKey] = S3FileKey(path, s3FileKey)

  /**
    *
    * @param path
    *   Should not end in slash, or start in slash
    * @return
    *   given a key "folder" creates "folder/path"
    */
  def appendPath(path: String): Attempt[S3FileKey] = S3FileKey(s3FileKey, path)
}
