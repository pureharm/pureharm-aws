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
package busymachines.pureharm.aws.s3.internals

import busymachines.pureharm.aws.core._
import busymachines.pureharm.aws.s3._
import scala.concurrent.duration.FiniteDuration

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  */
private[s3] object PureJavaS3 {

  import software.amazon.awssdk.auth.credentials._
  import software.amazon.awssdk.core.client.config._
  import software.amazon.awssdk.services.s3.{S3AsyncClient, S3Configuration}

  def buildClient(config: S3Config): S3AsyncClient = {
    val awsCredentials: AwsBasicCredentials = AwsBasicCredentials.create(
      config.accessKeyID,
      config.secretAccessKey,
    )

    val aWSCredentialsProvider: AwsCredentialsProvider =
      StaticCredentialsProvider.create(awsCredentials)

    val asyncConfig: ClientAsyncConfiguration = ClientAsyncConfiguration.builder().build()

    val s3Config: S3Configuration =
      S3Configuration.builder().build()

    import scala.jdk.CollectionConverters._

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
      .region(config.region.toSDKRegion)
      .serviceConfiguration(s3Config)
      .asyncConfiguration(asyncConfig)
      .overrideConfiguration(overrideConfiguration)

    val client: S3AsyncClient = config.endpointOverride match {
      case None           => builder.build()
      case Some(endpoint) => builder.endpointOverride(endpoint).build()
    }
    client
  }

  implicit private class FJInterop(fd: FiniteDuration) {
    def asJava: java.time.Duration = java.time.Duration.ofNanos(fd.toNanos)
  }
}
