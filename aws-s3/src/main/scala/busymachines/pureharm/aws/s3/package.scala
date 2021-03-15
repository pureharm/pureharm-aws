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
package busymachines.pureharm.aws

import busymachines.pureharm.phantom._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import cats.MonadThrow

import scala.concurrent.duration.FiniteDuration

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  */
package object s3 {

  type S3FileKey = S3FileKey.Type

  object S3FileKey
    extends SproutRefinedSubThrow[String] with SproutShow[String] with SproutEq[String] with SproutOrder[String] {
    import java.nio.file.{Path, Paths}

    private def randomString[F[_]: Sync]: F[String] =
      Sync[F].delay(Math.abs(scala.util.Random.nextLong()).toString.padTo(9, '0'))

    def generate[F[_]: Sync]: F[this.Type] =
      randomString[F].flatMap(r => this.newType[F](r))

    /** @param extension
      *   Should not contain "."
      * @return
      *   Randomly generated name w/ the given extension
      */
    def generate[F[_]: Sync](extension: String): F[this.Type] =
      randomString[F].flatMap(r => this.newType[F](s"$r.$extension"))

    def apply[F[_]](fst: String, snd: String, rest: String*)(implicit m: MonadThrow[F]): F[this.Type] =
      this.apply[F](s"$fst/$snd/${rest.mkString("", "/", "")}")

    override def refine[F[_]](o: String)(implicit m: MonadThrow[F]): F[String] =
      validatePath[F](o).map(_.normalize().toString)

    final def asJPath[F[_]: MonadThrow](s3FilePath: this.Type): F[Path] =
      MonadThrow[F].catchNonFatal(Paths.get(this.oldType(s3FilePath))).adaptError { case NonFatal(e) =>
        InvalidPathIA(s3FilePath, e)
      }

    final def asJPathUnsafe(s3FilePath: this.Type): Path =
      asJPath[Attempt](s3FilePath).unsafeGet()

    private def validatePath[F[_]: MonadThrow](path: String): F[Path] =
      MonadThrow[F].catchNonFatal(Paths.get(path)).adaptError { case NonFatal(e) => InvalidPathIA(path, e) }

    implicit final class S3FileKeyOps(val s3FileKey: S3FileKey) extends AnyVal {

      /** @param path
        *   Should not end or start in slash
        * @return
        *   given a key "x.foo" creates "$path/x.foo"
        */
      def prependPath[F[_]: MonadThrow](path: String): F[S3FileKey] = S3FileKey[F](path, s3FileKey)

      /** @param path
        *   Should not end in slash, or start in slash
        * @return
        *   given a key "folder" creates "folder/path"
        */
      def appendPath[F[_]: MonadThrow](path: String): F[S3FileKey] = S3FileKey[F](s3FileKey, path)
    }

  }

  type S3Path = S3Path.Type

  object S3Path
    extends SproutRefinedSubThrow[String] with SproutShow[String] with SproutEq[String] with SproutOrder[String] {
    import java.nio.file._

    def apply[F[_]: MonadThrow](fst: String, snd: String, rest: String*): F[S3Path] =
      this.apply[F](s"$fst/$snd/${rest.mkString("", "/", "")}")

    override def refine[F[_]: MonadThrow](o: String): F[String] =
      validatePath[F](o).map(_.normalize().toString)

    final def asJPath[F[_]: MonadThrow](s3FilePath: S3Path): F[Path] =
      MonadThrow[F].catchNonFatal(Paths.get(this.oldType(s3FilePath))).adaptError { case e =>
        InvalidPathIA(s3FilePath, e)
      }

    private def validatePath[F[_]: MonadThrow](path: String): F[Path] =
      MonadThrow[F].catchNonFatal(Paths.get(path)).adaptError { case e => InvalidPathIA(path, e) }
  }

  object S3BinaryContent extends SproutSub[Array[Byte]]
  type S3BinaryContent = S3BinaryContent.Type

  type S3BinaryStream[F[_]] = fs2.Stream[F, Byte]

  object S3Bucket extends SproutSub[String]
  type S3Bucket = S3Bucket.Type

  object S3AccessKeyID extends SproutSub[String]
  type S3AccessKeyID = S3AccessKeyID.Type

  object S3SecretAccessKey extends SproutSub[String]
  type S3SecretAccessKey = S3SecretAccessKey.Type

  object S3ApiCallAttemptTimeout extends SproutSub[FiniteDuration]
  type S3ApiCallAttemptTimeout = S3ApiCallAttemptTimeout.Type

  object S3ApiCallTimeout extends SproutSub[FiniteDuration]
  type S3ApiCallTimeout = S3ApiCallTimeout.Type

  object S3DownloadURL extends SproutSub[String]
  type S3DownloadURL = S3DownloadURL.Type

  object S3EndpointOverride extends SproutSub[java.net.URI]
  type S3EndpointOverride = S3EndpointOverride.Type

}
