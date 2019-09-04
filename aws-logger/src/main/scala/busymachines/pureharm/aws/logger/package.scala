package busymachines.pureharm.aws

import busymachines.pureharm.phantom.PhantomType

import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04 Sep 2019
  *
  */
package object logger {

  object AWSLoggingEnabled extends PhantomType[Boolean] {
    val True:  this.Type = this(true)
    val False: this.Type = this(false)
  }
  type AWSLoggingEnabled = AWSLoggingEnabled.Type

  object CloudWatchTimeoutDuration extends PhantomType[FiniteDuration]
  type CloudWatchTimeoutDuration = CloudWatchTimeoutDuration.Type

  object CloudWatchAccessKeyID extends PhantomType[String]
  type CloudWatchAccessKeyID = CloudWatchAccessKeyID.Type

  object CloudWatchSecretAccessKey extends PhantomType[String]
  type CloudWatchSecretAccessKey = CloudWatchSecretAccessKey.Type
}
