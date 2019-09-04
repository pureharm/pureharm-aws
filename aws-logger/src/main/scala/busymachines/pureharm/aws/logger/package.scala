package busymachines.pureharm.aws

import busymachines.pureharm.phantom.PhantomType

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

}
