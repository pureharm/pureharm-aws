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

import busymachines.pureharm.phantom._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  */

object S3FileKey extends SafePhantomType[Throwable, String] {
  import java.nio.file.{Path, Paths}

  private def randomString[F[_]: Sync]: F[String] =
    Sync[F].delay(Math.abs(scala.util.Random.nextLong()).toString.padTo(9, '0'))

  def generate[F[_]: Sync]: F[this.Type] =
    randomString[F].map(r => this.unsafe(r))

  /** @param extension
    *   Should not contain "."
    * @return
    *   Randomly generated name w/ the given extension
    */
  def generate[F[_]: Sync](extension: String): F[this.Type] =
    randomString[F].map(r => this.unsafe(s"$r.$extension"))

  def apply(fst: String, snd: String, rest: String*): Attempt[this.Type] =
    this.apply(s"$fst/$snd/${rest.mkString("", "/", "")}")

  override def check(value: String): Attempt[String] =
    validatePath(value).map(_.normalize().toString)

  final def asJPath(s3FilePath: this.Type): Attempt[Path] =
    Attempt.catchNonFatal(Paths.get(this.despook(s3FilePath))).adaptError { case NonFatal(e) =>
      InvalidPathIA(s3FilePath, e)
    }

  final def asJPathUnsafe(s3FilePath: this.Type): Path =
    asJPath(s3FilePath).unsafeGet()

  private def validatePath(path: String): Attempt[Path] =
    Attempt.catchNonFatal(Paths.get(path)).adaptError { case NonFatal(e) => InvalidPathIA(path, e) }

}
