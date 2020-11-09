package busymachines.pureharm.aws.core

import busymachines.pureharm.config._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 04 Sep 2019
  */
object implicits {

  implicit val pureharmAmazonRegionConfigReader: ConfigReader[AmazonRegion.Type] =
    AmazonRegion.pureharmAmazonRegionConfigReader

}
