package busymachines.pureharm.aws.core

import busymachines.pureharm.phantom._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
object AmazonRegion extends SafePhantomType[Throwable, String] {

  import software.amazon.awssdk.regions.Region
  import scala.jdk.CollectionConverters._

  private val validRegionStrings: Set[String] =
    Region.regions().asScala.map(_.id()).toSet

  override def check(value: String): Attempt[String] =
    if (validRegionStrings.contains(value))
      Attempt.pure(value)
    else Attempt.raiseError(InvalidAmazonS3RegionIA(value))

  private[aws] def toSDKRegion(ar: AmazonRegion.Type): Region = Region.of(ar)
}
