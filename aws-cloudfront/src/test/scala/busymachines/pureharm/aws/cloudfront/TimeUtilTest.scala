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
import scala.concurrent.duration._

import org.scalatest.funsuite.AnyFunSuite

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 05 May 2020
  */
class TimeUtilTest extends AnyFunSuite {

  test("compute expiration date compared to 'now'") {

    val delta     = 2.seconds
    val expiresIn = 5.minutes

    val t = for {
      result <- TimeUtil.computeExpirationDate[IO](CloudfrontURLExpiration(expiresIn))
      now    <- IO(new java.util.Date())
      diff = Duration(Math.abs((now.getTime + expiresIn.toMillis) - result.getTime), MILLISECONDS)
    } yield assert(diff <= delta)

    t.unsafeRunSync()

  }
}
