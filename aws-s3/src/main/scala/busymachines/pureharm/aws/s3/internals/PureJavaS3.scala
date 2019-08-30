package busymachines.pureharm.aws.s3.internals

import busymachines.pureharm.aws.core._
import busymachines.pureharm.aws.s3._
import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
private[s3] object PureJavaS3 {

  import software.amazon.awssdk.auth.credentials._
  import software.amazon.awssdk.core.client.config._
  import software.amazon.awssdk.services.s3.{S3AsyncClient, S3Configuration}

  def buildClient(config: S3Config): S3AsyncClient = {
    val awsCredentials: AwsBasicCredentials = AwsBasicCredentials.create(
      config.s3AccessKeyId,
      config.secretAccessKey,
    )

    val aWSCredentialsProvider: AwsCredentialsProvider =
      StaticCredentialsProvider.create(awsCredentials)

    val asyncConfig: ClientAsyncConfiguration = ClientAsyncConfiguration.builder().build()

    val s3Config: S3Configuration =
      S3Configuration.builder().build()

    import scala.jdk.CollectionConverters._
    import scala.collection.compat._ //FIXME: once we're only on scala 2.13 this is no longer necessary

    val headers: java.util.Map[String, java.util.List[String]] =
      config.headers.groupBy(_.name).view.mapValues(_.map(_.value).asJava).toMap.asJava

    val overrideConfiguration: ClientOverrideConfiguration =
      ClientOverrideConfiguration
        .builder()
        .apiCallAttemptTimeout(config.apiCallAttemptTimeout.asJava)
        .apiCallTimeout(config.apiCallTimeout.asJava)
        .headers(headers)
        .build()

    val builder = S3AsyncClient
      .builder()
      .credentialsProvider(aWSCredentialsProvider)
      .region(AmazonRegion.toSDKRegion(config.region))
      .serviceConfiguration(s3Config)
      .asyncConfiguration(asyncConfig)
      .overrideConfiguration(overrideConfiguration)

    val client: S3AsyncClient = builder.build()
    client
  }

  implicit private class FJInterop(fd: FiniteDuration) {
    def asJava: java.time.Duration = java.time.Duration.ofNanos(fd.toNanos)
  }
}
