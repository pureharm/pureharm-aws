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

package busymachines.pureharm.aws

import busymachines.pureharm.sprout._

import scala.concurrent.duration.FiniteDuration

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04
  *   Sep 2019
  */
package object logger {

  type AWSLoggerFactory[F[_]] = AWSLogging[F]

  object AWSLoggingEnabled extends SproutSub[Boolean] {
    val True:  this.Type = this(true)
    val False: this.Type = this(false)
  }
  type AWSLoggingEnabled = AWSLoggingEnabled.Type

  object CloudWatchTimeoutDuration extends SproutSub[FiniteDuration]
  type CloudWatchTimeoutDuration = CloudWatchTimeoutDuration.Type

  object CloudWatchAccessKeyID extends SproutSub[String]
  type CloudWatchAccessKeyID = CloudWatchAccessKeyID.Type

  object CloudWatchSecretAccessKey extends SproutSub[String]
  type CloudWatchSecretAccessKey = CloudWatchSecretAccessKey.Type

  object CloudWatchGroupName extends SproutSub[String]
  type CloudWatchGroupName = CloudWatchGroupName.Type

  object CloudWatchStreamName extends SproutSub[String]
  type CloudWatchStreamName = CloudWatchStreamName.Type
}
