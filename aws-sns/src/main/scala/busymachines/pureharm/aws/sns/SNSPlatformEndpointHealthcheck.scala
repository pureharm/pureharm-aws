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

package busymachines.pureharm.aws.sns

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 29 Nov 2019
  */
sealed trait SNSPlatformEndpointHealthcheck extends Product with Serializable

object SNSPlatformEndpointHealthcheck {
  /** @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[sns] val TokenAttributeID   = "Token"
  /** @see
    *   https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html#mobile-platform-endpoint-sdk-examples
    */
  private[sns] val EnabledAttributeID = "Enabled"

  //facepalm
  private[sns] val TrueString = "true"

  case object Exists   extends SNSPlatformEndpointHealthcheck
  case object NotFound extends SNSPlatformEndpointHealthcheck
  case object Disabled extends SNSPlatformEndpointHealthcheck
}
