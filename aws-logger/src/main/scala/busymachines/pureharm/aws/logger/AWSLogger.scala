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
/** Logger that also logs to AWS, not only to the local machine.
  *
  * Can only be built using the [[AWSLoggerFactory]] for
  * thread safety, and resource safety reasons
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 09 Apr 2019
  */
sealed trait AWSLogger[F[_]] extends SelfAwareStructuredLogger[F]

object AWSLogger {
  import busymachines.pureharm.aws.logger.internals.AWSRemoteLoggerImpl

  /** Logger that uses standard SFL4j stuff to log locally,
    * and then, logs to amazon as well.
    *
    * @author Lorand Szakacs, https://github.com/lorandszakacs
    * @since 09 Apr 2019
    */
  final private[logger] class AWSLoggerImpl[F[_]: Sync](
    private val remote: AWSRemoteLoggerImpl[F],
    private val local:  SelfAwareStructuredLogger[F],
  ) extends AWSLogger[F] {

    import busymachines.pureharm.effects.implicits._

    override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
      local.trace(ctx)(msg) *> remote.trace(ctx)(msg)

    override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.trace(ctx, t)(msg) *> remote.trace(ctx, t)(msg)

    override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
      local.debug(ctx)(msg) *> remote.debug(ctx)(msg)

    override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.debug(ctx, t)(msg) *> remote.debug(ctx, t)(msg)

    override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
      local.info(ctx)(msg) *> remote.info(ctx)(msg)

    override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.info(ctx, t)(msg) *> remote.info(ctx, t)(msg)

    override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
      local.warn(ctx)(msg) *> remote.warn(ctx)(msg)

    override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.warn(ctx, t)(msg) *> remote.warn(ctx, t)(msg)

    override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
      local.error(ctx)(msg) *> remote.error(ctx)(msg)

    override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.error(ctx, t)(msg) *> remote.error(ctx, t)(msg)

    override def error(t: Throwable)(message: => String): F[Unit] =
      local.error(t)(message) *> remote.error(t)(message)

    override def warn(t: Throwable)(message: => String): F[Unit] =
      local.warn(t)(message) *> remote.warn(t)(message)

    override def info(t: Throwable)(message: => String): F[Unit] =
      local.info(t)(message) *> remote.info(t)(message)

    override def debug(t: Throwable)(message: => String): F[Unit] =
      local.debug(t)(message) *> remote.debug(t)(message)

    override def trace(t: Throwable)(message: => String): F[Unit] =
      local.trace(t)(message) *> remote.trace(t)(message)

    override def error(message: => String): F[Unit] =
      local.error(message) *> remote.error(message)

    override def warn(message: => String): F[Unit] =
      local.warn(message) *> remote.warn(message)

    override def info(message: => String): F[Unit] =
      local.info(message) *> remote.info(message)

    override def debug(message: => String): F[Unit] =
      local.debug(message) *> remote.debug(message)

    override def trace(message: => String): F[Unit] =
      local.trace(message) *> remote.trace(message)

    override def isTraceEnabled: F[Boolean] = local.isTraceEnabled

    override def isDebugEnabled: F[Boolean] = local.isDebugEnabled

    override def isInfoEnabled: F[Boolean] = local.isInfoEnabled

    override def isWarnEnabled: F[Boolean] = local.isWarnEnabled

    override def isErrorEnabled: F[Boolean] = local.isErrorEnabled
  }

  /** Unfortunately we can't do PhantomTypes for Logger[F] :(
    * so we have to be really lame about just delegating to a
    * logger
    *
    * @author Lorand Szakacs, https://github.com/lorandszakacs
    * @since 23 Apr 2019
    */
  final private[logger] class DummyAWSLoggerImpl[F[_]](
    private val local: SelfAwareStructuredLogger[F]
  ) extends AWSLogger[F] {

    @inline override def trace(ctx: Map[String, String])(msg: => String): F[Unit] = local.trace(ctx)(msg)

    @inline override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.trace(ctx, t)(msg)

    @inline override def debug(ctx: Map[String, String])(msg: => String): F[Unit] = local.debug(ctx)(msg)

    @inline override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.debug(ctx, t)(msg)

    @inline override def info(ctx: Map[String, String])(msg: => String): F[Unit] = local.info(ctx)(msg)

    @inline override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = local.info(ctx, t)(msg)

    @inline override def warn(ctx: Map[String, String])(msg: => String): F[Unit] = local.warn(ctx)(msg)

    @inline override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = local.warn(ctx, t)(msg)

    @inline override def error(ctx: Map[String, String])(msg: => String): F[Unit] = local.error(ctx)(msg)

    @inline override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
      local.error(ctx, t)(msg)

    @inline override def error(t: Throwable)(message: => String): F[Unit] = local.error(t)(message)

    @inline override def warn(t: Throwable)(message: => String): F[Unit] = local.warn(t)(message)

    @inline override def info(t: Throwable)(message: => String): F[Unit] = local.info(t)(message)

    @inline override def debug(t: Throwable)(message: => String): F[Unit] = local.debug(t)(message)

    @inline override def trace(t: Throwable)(message: => String): F[Unit] = local.trace(t)(message)

    @inline override def error(message: => String): F[Unit] = local.error(message)

    @inline override def warn(message: => String): F[Unit] = local.warn(message)

    @inline override def info(message: => String): F[Unit] = local.info(message)

    @inline override def debug(message: => String): F[Unit] = local.debug(message)

    @inline override def trace(message: => String): F[Unit] = local.trace(message)

    @inline override def isTraceEnabled: F[Boolean] = local.isTraceEnabled

    @inline override def isDebugEnabled: F[Boolean] = local.isDebugEnabled

    @inline override def isInfoEnabled: F[Boolean] = local.isInfoEnabled

    @inline override def isWarnEnabled: F[Boolean] = local.isWarnEnabled

    @inline override def isErrorEnabled: F[Boolean] = local.isErrorEnabled
  }

}
