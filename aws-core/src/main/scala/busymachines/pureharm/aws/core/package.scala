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

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import busymachines.pureharm.sprout._

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 11
  *   Jul 2019
  */
package object core {
  type AmazonRegion = AmazonRegion.Type

  object AmazonRegion extends SproutRefinedSubThrow[String] {
    import busymachines.pureharm.config._
    import software.amazon.awssdk.regions.Region
    import scala.jdk.CollectionConverters._

    private val validRegionStrings: Set[String] =
      Region.regions().asScala.map(_.id()).toSet

    override def refine[F[_]](o: String)(implicit m: MonadError[F, Throwable]): F[String] =
      if (validRegionStrings.contains(o)) o.pure[F] else InvalidAmazonRegionIA(o).raiseError[F, String]

    private[aws] def toSDKRegion(ar: AmazonRegion): Region = Region.of(ar)

    implicit private[aws] class RegionOps(val v: AmazonRegion) extends AnyVal {
      def toSDKRegion: Region = AmazonRegion.toSDKRegion(v)
      def asString:    String = oldType(v)
    }

    import pureconfig.error._

    implicit val pureharmAmazonRegionConfigReader: ConfigReader[AmazonRegion] =
      ConfigReader[String].emap(value => this.apply[Attempt](value).leftMap(ExceptionThrown.apply))
  }
}
