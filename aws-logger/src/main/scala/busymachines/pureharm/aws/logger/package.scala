package busymachines.pureharm.aws

import busymachines.pureharm.phantom._

import scala.concurrent.duration.FiniteDuration

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04 Sep 2019
  */
package object logger {

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
