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

package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 05 May 2020
  */
object TimeUtil {

  def computeExpirationDate[F[_]](expireIn: CloudfrontURLExpiration)(implicit F: Sync[F]): F[java.util.Date] =
    F.delay {
      val date   = new java.util.Date()
      val millis = date.getTime + expireIn.toMillis
      date.setTime(millis)
      date
    }

}
