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

package busymachines.pureharm.aws.core

import busymachines.pureharm.anomaly._

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10
  *   Jul 2019
  */
final case class InvalidAmazonRegionIA(r: String) extends InvalidInputAnomaly(s"Invalid Amazon region: $r") {
  override val id: AnomalyID = InvalidAmazonRegionID
}
case object InvalidAmazonRegionID extends AnomalyID { override val name: String = "PH_AWS_CORE_001" }
