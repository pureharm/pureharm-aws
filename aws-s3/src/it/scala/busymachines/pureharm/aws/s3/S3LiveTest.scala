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

import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.testkit._
import fs2._

/** --- IGNORED BY DEFAULT — test expects proper live amazon config ---
  *
  * Before running this ensure that you actually have the proper local environment
  * variables. See the ``pureharm-aws/aws-s3/src/test/resources/reference.conf``
  * for the environment variables that are used by this test.
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 22 May 2019
  */
final class S3LiveTest extends PureharmTestWithResource {
  private val UTF_8 = java.nio.charset.StandardCharsets.UTF_8
  implicit val l: StructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override type ResourceType = (S3Config, AmazonS3Client[IO])

  override def resource(meta: MetaData): Resource[IO, ResourceType] = for {
    config   <- S3Config.fromNamespaceR[IO]("test-live.pureharm.aws.s3")
    s3Client <- AmazonS3Client.resource[IO](config)
  } yield (config, s3Client)

  private val f1S3Key: S3FileKey =
    S3FileKey("folder", "subfolder", "file.txt").unsafeGet()

  private val f2S3Key: S3FileKey =
    S3FileKey("folder", "file_copy.txt").unsafeGet()

  private val f1_contents: S3BinaryContent = S3BinaryContent(
    "GOOGLE_MURRAY_BOOKCHIN".getBytes(java.nio.charset.StandardCharsets.UTF_8)
  )

  test("s3 put + get + delete") { case (config, client) =>
    for {
      _   <- l.info(s"acquired client resource: ${client.toString}")
      _   <-
        client
          .put(config.bucket, f1S3Key, f1_contents)
          .void
          .handleErrorWith(e => l.error(e)(s"PUT failed w/: $e"))
      _   <- l.info(s"1 — after PUT — trying GET")
      got <- client.get(config.bucket, f1S3Key)
      _   <- l.info(s"2 — after GET — we got back: ${new String(got, UTF_8)}")
      _   <- l.info(s"2 — after GET — we expect: ${new String(f1_contents, UTF_8)}")
      _   <- IO(assert(f1_contents.toList == got.toList)).onErrorF(l.info("comparison failed :(("))

      _ <- l.info("---- deleting file ----")
      _ <- client.delete(config.bucket, f1S3Key)
      _ <- l.info("---- DELETED — now trying to get back file to see----")
      _ <-
        client
          .get(config.bucket, f1S3Key)
          .flatMap(g => l.error(s"SHOULD HAVE DELETED, but got: ${new String(g, UTF_8)}"))
          .void
          .handleErrorWith(t => l.info(s"AFTER DELETE — expected failure, and got it: ${t.toString}"))
    } yield succeed
  }

  test("s3 putStream + getStream + delete") { case (config, client) =>
    for {
      _   <- l.info(s"acquired client resource: ${client.toString}")
      _   <-
        client
          .putStream(config.bucket, f1S3Key, Stream.emits[IO, Byte](f1_contents.toSeq))
          .void
          .handleErrorWith(e => l.error(e)(s"PUT stream failed w/: $e"))
      _   <- l.info(s"1 — after PUT — trying GET")
      got <- client.getStream(config.bucket, f1S3Key).compile.toList
      _   <- l.info(s"2 — after GET — we got back: ${new String(got.toArray, UTF_8)}")
      _   <- l.info(s"2 — after GET — we expect: ${new String(f1_contents, UTF_8)}")
      _   <- IO(assert(f1_contents.toList == got.toList)).onErrorF(l.info("comparison failed :(("))

      _ <- l.info("---- deleting file ----")
      _ <- client.delete(config.bucket, f1S3Key)
      _ <- l.info("---- DELETED — now trying to get back file to see----")
      _ <-
        client
          .get(config.bucket, f1S3Key)
          .flatMap(g => l.error(s"SHOULD HAVE DELETED, but got: ${new String(g, UTF_8)}"))
          .void
          .handleErrorWith(t => l.info(s"AFTER DELETE — expected failure, and got it: ${t.toString}"))
    } yield succeed
  }

  test("s3 copy + exists + list") { case (config, client) =>
    for {
      _      <- l.info(s"acquired client resource: ${client.toString}")
      _      <-
        client
          .put(config.bucket, f1S3Key, f1_contents)
          .void
          .handleErrorWith(e => l.error(e)(s"PUT failed w/: $e"))
      _      <- l.info(s"1 — trying COPY")
      _      <-
        client
          .copy(config.bucket, f1S3Key, config.bucket, f2S3Key)
          .void
          .handleErrorWith(e => l.error(e)(s"COPY failed w/: $e"))
      _      <- l.info(s"2 - after COPY - trying EXISTS")
      exists <-
        client
          .exists(config.bucket, f2S3Key)
          .handleErrorWith(e => l.error(e)(s"EXISTS failed w/: $e").map(_ => false))
      _      <- l.info(s"2 — after EXISTS — we got back: $exists")
      _      <- l.info(s"2 — after EXISTS — we expect: true")
      _      <- IO(assert(exists)).onErrorF(l.info("comparison failed :(("))
      _      <- l.info(s"3 - after EXISTS - trying LIST")
      listReqPrefix = S3Path.unsafe("folder")
      listResult <-
        client
          .list(config.bucket, listReqPrefix)
          .handleErrorWith(e => l.error(e)(s"LIST failed w/: $e").map(_ => List()))
      _          <- l.info(s"3 — after LIST — we got back: ${listResult.mkString("[", ",", "]")}")
      _          <- l.info(s"3 — after LIST — we expect: ${List(f1S3Key, f2S3Key).mkString("[", ",", "]")}")
      _          <- IO(assert(listResult.toSet == Set(f1S3Key, f2S3Key))).onErrorF(l.info("comparison failed :(("))
      _          <- l.info("---- cleanup ----")
      _          <-
        client
          .delete(config.bucket, f1S3Key)
          .handleErrorWith(e => l.error(e)(s"DELETE failed w/: $e"))
      _          <-
        client
          .delete(config.bucket, f2S3Key)
          .handleErrorWith(e => l.error(e)(s"DELETE failed w/: $e"))
    } yield succeed
  }

}
