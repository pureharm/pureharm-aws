package busymachines.pureharm.aws.s3.internals

import io.chrisdavenport.log4cats.StructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.s3._

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
//TODO: replace w/ a scalatest that we can ignore
object S3AppTest extends PureharmIOApp {
  private val UTF_8 = java.nio.charset.StandardCharsets.UTF_8
  implicit val l: StructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override val ioRuntime: Later[(ContextShift[IO], Timer[IO])] = IORuntime.defaultMainRuntime("s3-test")

  private val configR = Resource.liftF(S3Config.default[IO])

  implicit val ioEC: ExecutionContextCT = UnsafePools.cached("s3-test")

  implicit private val shifter: BlockingShifter[IO] = BlockingShifter.fromExecutionContext(ioEC)(contextShift)

  private val f1S3Key: S3FileKey =
    S3FileKey("folder", "subfolder", "file.txt").right.get

  private val f1_contents: S3BinaryContent = S3BinaryContent(
    "GOOGLE_MURRAY_BOOKCHIN".getBytes(java.nio.charset.StandardCharsets.UTF_8),
  )

  override def run(args: List[String]): IO[ExitCode] = {
    (testClient >>
      l.info("FINISHED!") >>
      ExitCode.Success.pure[IO]).handleErrorWith(e => l.error(e)(s"failed w/: $e").as(ExitCode.Error))

  }

  private def testClient: IO[Unit] = {
    val r = for {
      config <- configR
      client <- AmazonS3Client.resource[IO](config)
    } yield (config, client)

    r.use {
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
  }
}
