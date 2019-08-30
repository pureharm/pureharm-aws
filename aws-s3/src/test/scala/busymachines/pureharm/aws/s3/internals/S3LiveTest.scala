package busymachines.pureharm.aws.s3.internals

import io.chrisdavenport.log4cats.StructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.s3._
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  * Before running this ensure that you actually have the proper local environment
  * variables. See the reference.conf of this module for needs configuring.
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 22 May 2019
  *
  */
@org.scalatest.Ignore
final class S3LiveTest extends AnyFunSuite {
  private val UTF_8 = java.nio.charset.StandardCharsets.UTF_8
  private val ioRuntime:   Later[(ContextShift[IO], Timer[IO])] = IORuntime.defaultMainRuntime("s3-cf-test")
  implicit private val cs: ContextShift[IO]                     = ioRuntime.value._1

  implicit val l: StructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val s3clientR: Resource[IO, (S3Config, AmazonS3Client[IO])] =
    for {
      config     <- S3Config.defaultR[IO]
      blockingEC <- Pools.cached[IO]("aws-block")
      implicit0(b: BlockingShifter[IO]) <- BlockingShifter.fromExecutionContext[IO](blockingEC).pure[Resource[IO, ?]]
      s3Client <- AmazonS3Client.resource[IO](config)
    } yield (config, s3Client)

  private val f1S3Key: S3FileKey =
    S3FileKey("folder", "subfolder", "file.txt").right.get

  private val f1_contents: S3BinaryContent = S3BinaryContent(
    "GOOGLE_MURRAY_BOOKCHIN".getBytes(java.nio.charset.StandardCharsets.UTF_8),
  )

  test("s3 upload + get + delete") {
    val testIO = s3clientR.use {
      case (config, client) =>
        for {
          _ <- l.info(s"acquired client resource: ${client.toString}")
          _ <- client
            .put(config.bucket, f1S3Key, f1_contents)
            .void
            .handleErrorWith(e => l.error(e)(s"PUT failed w/: $e"))
          _   <- l.info(s"1 — after PUT — trying GET")
          got <- client.get(config.bucket, f1S3Key)
          _   <- l.info(s"2 — after GET — we got back: ${new String(got, UTF_8)}")
          _   <- l.info(s"2 — after GET — we expect: ${new String(f1_contents, UTF_8)}")
          _   <- IO(assert(f1_contents.deep == got.deep)).onErrorF(l.info("comparison failed :(("))

          _ <- l.info("---- deleting file ----")
          _ <- client.delete(config.bucket, f1S3Key)
          _ <- l.info("---- DELETED — now trying to get back file to see----")
          _ <- client
            .get(config.bucket, f1S3Key)
            .flatMap(g => l.error(s"SHOULD HAVE DELETED, but got: ${new String(g, UTF_8)}"))
            .void
            .handleErrorWith(t => l.info(s"AFTER DELETE — expected failure, and got it: ${t.toString}"))
        } yield ()
    }

    testIO.unsafeRunSync()
  }

}
