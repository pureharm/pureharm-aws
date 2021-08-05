package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.aws.s3._
import busymachines.pureharm.testkit._
import org.http4s.client.Client
import org.http4s.ember.client._
import org.typelevel.log4cats.slf4j.Slf4jLogger

/** --- IGNORED BY DEFAULT — test expects proper live amazon config ---
  *
  * Before running this ensure that you actually have the proper local environment variables. See the
  * ``pureharm-aws/aws-cloudfront/src/test/resources/application.conf`` for the environment variables that are used by
  * this test.
  *
  * We can't commit to github the proper configuration to make this run.
  *
  * GIVEN $REGION=?eu-west-1 or whatever The idea is:
  *   1. You need an S3 bucket without public access, call it $S3_BUCKET
  *      https://s3.console.aws.amazon.com/s3/home?region=$REGION 2. Create a cloudfront distribution that uses your S3
  *      bucket as the origin https://console.aws.amazon.com/cloudfront/home?region=$REGION#distributions: call it
  *      $CLOUDFRONT_DIST 3. FOR NOW, make sure that the "Behaviors" tab of the above $CLOUDFRONT_DIST the trusted
  *      signers is set to "no", "nothing", "none". For now, can't figure out why it doesn't work with it. 4. Create an
  *      Origin Access Identity(OAI)for $CLOUDFRONT_DIST, called $OAI
  *      https://console.aws.amazon.com/cloudfront/home?region=eu-central-1#oai: 4.1 and assign this OAI to have access
  *      to your S3. Either select the radio button "Yes, update bucket permissions" or do step 5, manually: 5. OPTIONAL
  *      — Make sure that your $S3_BUCKET bucket policies is configured to allow reads from your cloudfront distribution
  *      https://s3.console.aws.amazon.com/s3/buckets/$S3_BUCKET/?region=$REGION&tab=permissions
  *
  * It should looks something like:
  * {{{
  * {
  *     "Version": "2012-10-17",
  *     "Id": "PolicyForCloudFrontPrivateContent",
  *     "Statement": [
  *         {
  *             "Sid": " 1",
  *             "Effect": "Allow",
  *             "Principal": {
  *                 "AWS": "arn:aws:iam::cloudfront:user/CloudFront Origin Access Identity $OAI"
  *             },
  *             "Action": "s3:GetObject",
  *             "Resource": "arn:aws:s3:::$S3_BUCKET/$STAR_CHAR_CANNOT_USE_IT_BECAUSE_COMMENT"
  *         }
  *     ]
  * }
  * }}}
  *
  * Then, ensure that your root user for the amazon account, created a cloudfront key/pair ID, and provides you with its
  * id, required for the [[busymachines.pureharm.aws.cloudfront.CloudfrontKeyPairID]]. Additionally, make sure you have
  * the private key file stored somewhere on your hard-drive, specified using the
  * [[busymachines.pureharm.aws.cloudfront.CloudfrontPrivateKeyFilePath]].
  *
  * @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19
  *   Jul 2019
  */
final class CloudfrontLiveURLSigningTestKeyPath extends PureharmTest {
  implicit override val testLogger: TestLogger = TestLogger(Slf4jLogger.getLogger[IO])
  val l = testLogger

  private type ResourceType = (Client[IO], S3Config, AmazonS3Client[IO], CloudfrontURLSigner[IO])

  private val resource = ResourceFixture[ResourceType] { _ =>
    for {
      config      <- (??? : Resource[IO, S3Config])
      //config      <- S3Config.fromNamespaceR[IO]("test-live.pureharm.aws.s3")
      emberClient <- EmberClientBuilder.default[IO].build
      s3Client    <- AmazonS3Client.resource[IO](config)
      // cfConfig    <- CloudfrontConfig.fromNamespaceR[IO]("test-live.pureharm.aws.cloudfront-key-path")
      cfConfig    <- (??? : Resource[IO, CloudfrontConfig])
      _           <- Resource.eval(l.info(s"CFCONFIG: $cfConfig"))
      cfClient    <- CloudfrontURLSigner[IO](cfConfig)
    } yield (emberClient, config, s3Client, cfClient)
  }

  private val s3KeyIO: IO[S3FileKey] = S3FileKey[IO]("aws_live_test", "subfolder", "google_murray_bookchin.txt")

  private val fileContent: S3BinaryContent = S3BinaryContent(
    "GOOGLE_MURRAY_BOOKCHIN".getBytes(java.nio.charset.StandardCharsets.UTF_8)
  )

  resource.test("s3 upload + signed url delivery via cloudfront") { case (http4sClient, s3Config, s3Client, cfSigner) =>
    for {
      s3Key <- s3KeyIO
      _     <- l.info(s"Uploaded file to s3: $s3Key")
      _     <-
        s3Client
          .put(s3Config.bucket, s3Key, fileContent)
          .onError { case e => l.error(e)(s"PUT failed w/: $e") }
          .void
          .handleErrorWith(_ => IO(fail("1. failed to upload file to s3")))

      checkingFile <- s3Client.get(s3Config.bucket, s3Key)
      _            <- l.info(s"Fetched file from s3: $s3Key")
      _            <- IO(assert(fileContent.toList == checkingFile.toList))
      _            <- l.info(s"File from s3 is all OK: $s3Key")

      signedURL       <- cfSigner.signS3KeyCanned(s3Key)
      _               <- l.info(s"Signed url: $signedURL")
      bytesFromSigned <- http4sClient.get(signedURL)(response => response.body.compile.toList)
      _               <- l.info(s"Fetched  #${bytesFromSigned.size} bytes from: GET $signedURL")
      _               <- l.info(s"Expected #${fileContent.size} bytes")
      _               <- IO(assert(fileContent.toList == bytesFromSigned))
    } yield ()
  }

}
