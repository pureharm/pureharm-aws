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
import fs2._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.testkit._

/** —-- test expects a minIO server to run on localport 9000 ---
  *
  * Before running this ensure that you actually have started the local
  * docker
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 22 May 2019
  */
final class S3MinIOTest extends PureharmTestWithResource {
  private val UTF_8 = java.nio.charset.StandardCharsets.UTF_8
  implicit val l: StructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override type ResourceType = (S3Config, AmazonS3Client[IO])

  override def resource(meta: MetaData): Resource[IO, ResourceType] = for {
    config   <- S3Config.fromNamespaceR[IO]("test-live.pureharm.aws.s3.minio")
    s3Client <- AmazonS3Client.resource[IO](config)
    _        <- Resource.liftF(l.info(s"creating minio bucket"))
    _        <- Resource.liftF(s3Client.deleteBucket(config.bucket).attempt.void) // just in case
    _        <- Resource.make(s3Client.initBucket(config.bucket))(_ => s3Client.deleteBucket(config.bucket))
    _        <- Resource.liftF(l.info(s"created minio bucket"))
  } yield (config, s3Client)

  private val f1S3Key: S3FileKey =
    S3FileKey[Try]("folder", "subfolder", "file.txt").get

  private val f2S3Key: S3FileKey =
    S3FileKey[Try]("folder", "file_copy.txt").get

  private val f1_contents: S3BinaryContent = S3BinaryContent(
    "GOOGLE_MURRAY_BOOKCHIN".getBytes(java.nio.charset.StandardCharsets.UTF_8)
  )

  private val f1_content_length: S3ContentLengthBytes = S3ContentLengthBytes(22L)

  test("minio init bucket + list bucket") { case (config, client) =>
    val newBucket = S3Bucket("testinitbucket")
    for {
      buckets <- client.listBuckets
      _ = assert(buckets.contains(config.bucket))
      att1: Attempt[Unit] <- client.initBucket(config.bucket).attempt
      _ = assertSuccess(att1)(())
      _ <- Resource.make(client.initBucket(newBucket))(_ => client.deleteBucket(newBucket).attempt.void).use { _ =>
        client.listBuckets.map(bs => IO(assert(bs.contains(newBucket))))
      }
    } yield succeed
  }

  test("minio put + get + delete") { case (config, client) =>
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

      metadata <- client.getMetadata(config.bucket, f1S3Key)
      _        <- l.info(s"3 — after GET metadata — we got back: $metadata")
      _        <- IO(assert(metadata.contentLength == f1_content_length, "content length"))

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

  test("minio putStream + getStream + delete") { case (config, client) =>
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

  test("minio copy + exists + list") { case (config, client) =>
    for {
      _             <- l.info(s"acquired client resource: ${client.toString}")
      _             <-
        client
          .put(config.bucket, f1S3Key, f1_contents)
          .void
          .handleErrorWith(e => l.error(e)(s"PUT failed w/: $e"))
      _             <- l.info(s"1 — trying COPY")
      _             <-
        client
          .copy(config.bucket, f1S3Key, config.bucket, f2S3Key)
          .void
          .handleErrorWith(e => l.error(e)(s"COPY failed w/: $e"))
      _             <- l.info(s"2 - after COPY - trying EXISTS")
      exists        <-
        client
          .exists(config.bucket, f2S3Key)
          .handleErrorWith(e => l.error(e)(s"EXISTS failed w/: $e").map(_ => false))
      _             <- l.info(s"2 — after EXISTS — we got back: $exists")
      _             <- l.info(s"2 — after EXISTS — we expect: true")
      _             <- IO(assert(exists)).onErrorF(l.info("comparison failed :(("))
      _             <- l.info(s"3 - after EXISTS - trying LIST")
      listReqPrefix <- S3Path[IO]("folder")
      listResult    <-
        client
          .list(config.bucket, listReqPrefix)
          .handleErrorWith(e => l.error(e)(s"LIST failed w/: $e").map(_ => List()))
      _             <- l.info(s"3 — after LIST — we got back: ${listResult.mkString("[", ",", "]")}")
      _             <- l.info(s"3 — after LIST — we expect: ${List(f1S3Key, f2S3Key).mkString("[", ",", "]")}")
      _             <- IO(assert(listResult.toSet == Set(f1S3Key, f2S3Key))).onErrorF(l.info("comparison failed :(("))
      _             <- l.info("---- cleanup ----")
      _             <-
        client
          .delete(config.bucket, f1S3Key)
          .handleErrorWith(e => l.error(e)(s"DELETE failed w/: $e"))
      _             <-
        client
          .delete(config.bucket, f2S3Key)
          .handleErrorWith(e => l.error(e)(s"DELETE failed w/: $e"))
    } yield succeed
  }

}
