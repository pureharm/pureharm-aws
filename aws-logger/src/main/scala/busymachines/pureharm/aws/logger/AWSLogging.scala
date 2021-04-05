/*
 * Copyright 2019 BusyMachines
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

package busymachines.pureharm.aws.logger

import busymachines.pureharm.effects._
import org.typelevel.log4cats.SelfAwareStructuredLogger

/** Because building these amazon thingies is not thread safe,
  * we have to build a thread safe builder
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Apr 2019
  */
sealed trait AWSLogging[F[_]] {
  def getLogger(logger: SelfAwareStructuredLogger[F]): AWSLogger[F]
}

object AWSLogging {

  def getLogger[F[_]](localLogger: SelfAwareStructuredLogger[F])(implicit inst: AWSLogging[F]): AWSLogger[F] =
    inst.getLogger(localLogger)

  def apply[F[_]](implicit inst: AWSLogging[F]): AWSLogging[F] = inst

  def resource[F[_]: Concurrent: Timer: BlockingShifter](
    config: AWSLoggerConfig
  ): Resource[F, AWSLogging[F]] =
    config match {
      case enabled: EnabledAWSLoggerConfig =>
        for {
          awsLogs <- awsLogsResource(enabled.someCloudwatch)
        } yield new AWSLoggerFactoryImpl[F](
          awsLogs = awsLogs,
          config  = enabled.someCloudwatch,
        ): AWSLogging[F]
      case DisabledAWSLoggerConfig => dummyLFResource
    }

  def local[F[_]: Sync]: AWSLogging[F] = new DummyLoggerFactory[F]

  import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
  import com.amazonaws.services.logs.AWSLogsAsyncClientBuilder
  import com.amazonaws.services.logs.AWSLogsAsync

  private def awsLogsResource[F[_]: Sync](c: CloudWatchLoggerConfig): Resource[F, AWSLogsAsync] =
    Resource.make(awsLogsAsync(c))((b: AWSLogsAsync) => Sync[F].delay(b.shutdown()))

  private def awsLogsAsync[F[_]:    Sync](c: CloudWatchLoggerConfig): F[AWSLogsAsync]           = Sync[F].delay {
    lazy val awsCredentials         = new BasicAWSCredentials(c.accessKeyID, c.secretAccessKey)
    lazy val aWSCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials)
    val lc                          = AWSLogsAsyncClientBuilder
      .standard()
      .withRegion(c.region.asString)
      .withCredentials(aWSCredentialsProvider)
      .build()
    lc
  }

  private def dummyLFResource[F[_]: Sync]: Resource[F, AWSLogging[F]] =
    Resource.pure(new DummyLoggerFactory[F])

  private class DummyLoggerFactory[F[_]](implicit private val F: Sync[F]) extends AWSLogging[F] {

    override def getLogger(localLogger: SelfAwareStructuredLogger[F]): AWSLogger[F] =
      new AWSLogger.DummyAWSLoggerImpl[F](localLogger)
  }

  private class AWSLoggerFactoryImpl[F[_]: Concurrent: Timer: BlockingShifter](
    private val awsLogs: AWSLogsAsync,
    private val config:  CloudWatchLoggerConfig,
  ) extends AWSLogging[F] {
    import busymachines.pureharm.aws.logger.internals.AWSRemoteLoggerImpl

    override def getLogger(localLogger: SelfAwareStructuredLogger[F]): AWSLogger[F] = {
      val remote = AWSRemoteLoggerImpl[F](
        config      = config,
        localLogger = localLogger,
        awsLogs     = awsLogs,
      )

      new AWSLogger.AWSLoggerImpl[F](remote = remote, local = localLogger)
    }
  }
}
