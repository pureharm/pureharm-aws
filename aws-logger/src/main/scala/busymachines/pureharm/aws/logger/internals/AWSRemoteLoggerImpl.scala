/**
  * Copyright (c) 2017-2019 BusyMachines
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
package busymachines.pureharm.aws.logger.internals

import busymachines.pureharm.aws.logger._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

/**
  * Logger that only logs to remote (except failures while trying
  * to connect to AWS, which it logs locally).
  *
  * Composed with a fully local logger to implement an [[busymachines.pureharm.aws.logger.AWSLogger]]
  *
  * @param localLogger
  *   used to log any failures of logging to the cloud
  *
  * -----
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Apr 2019
  */
final private[logger] class AWSRemoteLoggerImpl[F[_]] private[logger] (
  private val config:      CloudWatchLoggerConfig,
  private val localLogger: SelfAwareStructuredLogger[F],
  private val remote:      AWSRemoteLoggerImpl.AWSHelper[F],
)(
  implicit private val F:  Concurrent[F],
  implicit private val cs: BlockingShifter[F],
) extends SelfAwareStructuredLogger[F] {
  import AWSRemoteLoggerImpl.Level

  override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Trace, ctx, msg)

  override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Trace, t, ctx, msg)

  override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Debug, ctx, msg)

  override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Debug, t, ctx, msg)

  override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Info, ctx, msg)

  override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Info, t, ctx, msg)

  override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Warn, ctx, msg)

  override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Warn, t, ctx, msg)

  override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Error, ctx, msg)

  override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    remote.logs2Cloud(Level.Error, t, ctx, msg)

  override def error(t: Throwable)(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Error, t, message)

  override def warn(t: Throwable)(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Warn, t, message)

  override def info(t: Throwable)(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Info, t, message)

  override def debug(t: Throwable)(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Debug, t, message)

  override def trace(t: Throwable)(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Trace, t, message)

  override def error(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Error, message)

  override def warn(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Warn, message)

  override def info(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Info, message)

  override def debug(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Debug, message)

  override def trace(message: => String): F[Unit] =
    remote.logs2Cloud(Level.Trace, message)

  override def isTraceEnabled: F[Boolean] = localLogger.isTraceEnabled

  override def isDebugEnabled: F[Boolean] = localLogger.isDebugEnabled

  override def isInfoEnabled: F[Boolean] = localLogger.isInfoEnabled

  override def isWarnEnabled: F[Boolean] = localLogger.isWarnEnabled

  override def isErrorEnabled: F[Boolean] = localLogger.isErrorEnabled
}

private[logger] object AWSRemoteLoggerImpl {

  import com.amazonaws.services.logs.AWSLogsAsync

  def apply[F[_]: Concurrent: Timer: BlockingShifter](
    config:      CloudWatchLoggerConfig,
    localLogger: SelfAwareStructuredLogger[F],
    awsLogs:     AWSLogsAsync,
  ): AWSRemoteLoggerImpl[F] =
    new AWSRemoteLoggerImpl(
      config      = config,
      localLogger = localLogger,
      remote      = new AWSHelper[F](config, localLogger, awsLogs),
    )

  sealed trait Level extends Product with Serializable

  object Level {
    case object Trace extends Level
    case object Debug extends Level
    case object Info  extends Level
    case object Warn  extends Level
    case object Error extends Level
  }

  import com.amazonaws.services.logs.model._
  import scala.jdk.CollectionConverters._

  final private[logger] class AWSHelper[F[_]: Timer](
    private val config:     CloudWatchLoggerConfig,
    private val logger:     SelfAwareStructuredLogger[F],
    private val awsLogs:    AWSLogsAsync,
  )(implicit private val F: Concurrent[F], private val shifter: BlockingShifter[F]) {

    private[logger] def logs2Cloud(l: Level, msg: String): F[Unit] =
      logWithLevel(l, msg)

    private[logger] def logs2Cloud(l: Level, t: Throwable, msg: String): F[Unit] =
      logWithLevel(l, s"$msg >>> reason: ${t.getLocalizedMessage} <<<")

    private[logger] def logs2Cloud(l: Level, ctx: Map[String, String], msg: String): F[Unit] =
      logWithLevel(l, s"$msg ** ${mkString(ctx)} **")

    private[logger] def logs2Cloud(l: Level, t: Throwable, ctx: Map[String, String], msg: String): F[Unit] =
      logWithLevel(l, s"$msg >>> reason: ${t.getLocalizedMessage} <<< ${mkString(ctx)}")

    private def mkString(ctx: Map[String, String]): String = ctx.mkString("{", ",", "}")

    //TODO: maybe this should put messages in a queue and only call logs2Cloud on a larger List
    //TODO: queue emptied at fixed size, or at fixed intervals
    //TODO: implement this in second iteration
    private def logWithLevel(l: Level, message: String): F[Unit] =
      for {
        //TODO: create time module
        now <- F.delay(java.time.ZonedDateTime.now().toInstant.toEpochMilli)
        log = new InputLogEvent().withTimestamp(now).withMessage(s"[${l.productPrefix}] $message")
        _ <- logToClouds(List(log))
      } yield ()

    /**
      * FIXME: Super rough implementation. REVISE
      * Requires actual study of AWS logging to see if there's
      * anything to be improved here.
      */
    private def logToClouds(logs: List[InputLogEvent]): F[Unit] = {
      val logF: F[Unit] =
        for {
          logDesc  <- describeLogStreams
          tokenOpt <- getUploadSequenceToken(logDesc)
          plrq: PutLogEventsRequest = tokenOpt match {
            case Some(tk) =>
              new PutLogEventsRequest(config.groupName, config.streamName, logs.asJava)
                .withSequenceToken(tk)
            case None     =>
              new PutLogEventsRequest(config.groupName, config.streamName, logs.asJava)
                .withSequenceToken(null) //java :'(!
          }
          _        <- putLogsOnCloud(plrq).void
        } yield ()

      val nonFailingF = logF.timeout(config.timeout).recoverWith {
        case NonFatal(e) => logger.trace(e)("Failed to log to AWS Cloud Watch!")
      }

      //This is where the magic happens!
      //N.B. we start a fiber off of logF to ensure
      //forkAndForget semantics, and the shifter ensures
      //that the fiber shifts the work on the blocking IO pool
      shifter.blockOn(nonFailingF.start).void
    }

    private def describeLogStreams: F[DescribeLogStreamsResult] = {
      val req = new DescribeLogStreamsRequest()
        .withLogGroupName(config.groupName)
        .withLogStreamNamePrefix(config.streamName)
      F.delay(awsLogs.describeLogStreams(req))
    }

    private def getUploadSequenceToken(lsr: DescribeLogStreamsResult): F[Option[String]] = F.delay {
      lsr.getLogStreams.asScala.find(_.getLogStreamName == config.streamName).map(_.getUploadSequenceToken)
    }

    //TODO: maybe put the next sequence token in an MVar and take it from there...
    //TODO: but first you'd have to see if that's how the API is intended to be used...
    private def putLogsOnCloud(plrq: PutLogEventsRequest): F[PutLogEventsResult] =
      F.delay(awsLogs.putLogEvents(plrq))
  }

}
