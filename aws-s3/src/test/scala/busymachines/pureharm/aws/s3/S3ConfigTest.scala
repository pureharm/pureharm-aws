/** Copyright (c) 2017-2019 BusyMachines
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
package busymachines.pureharm.aws.s3

import busymachines.pureharm.config.ConfigAggregateAnomalies
import busymachines.pureharm.effects._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 14 May 2019
  */
final class S3ConfigTest extends AnyFunSuite with Matchers {

  test("... read config from 'production' reference.conf") {
    noException shouldBe thrownBy {
      S3Config.default[IO].unsafeRunSync()
    }
  }

  test("... test-1 — read correct s3 config") {
    noException shouldBe thrownBy {
      S3Config.fromNamespace[IO]("test-config.pureharm.aws.test-1.s3").unsafeRunSync()
    }
  }

  test("... test-2 — invalid amazon region") {
    val exp = the[ConfigAggregateAnomalies] thrownBy {
      S3Config.fromNamespace[IO]("test-config.pureharm.aws.test-2.s3").unsafeRunSync()
    }
    assert(exp.getLocalizedMessage.contains("Invalid Amazon region"))
  }

}
