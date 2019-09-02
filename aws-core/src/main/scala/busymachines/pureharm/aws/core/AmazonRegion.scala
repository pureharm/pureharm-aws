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
