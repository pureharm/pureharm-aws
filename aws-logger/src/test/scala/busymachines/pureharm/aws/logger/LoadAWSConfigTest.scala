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
package busymachines.pureharm.aws.logger

import busymachines.pureharm.effects._
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 14 May 2019
  *
  */
final class LoadAWSConfigTest extends AnyFunSuite with Matchers {

  test("... read config from local test reference.conf") {
    noException shouldBe thrownBy {
      AWSLoggerConfig.fromNamespace[IO]("test-config.pureharm.aws.logger").unsafeRunSync()
    }
  }

  test("... read config from default reference.conf") {
    noException shouldBe thrownBy {
      AWSLoggerConfig.default[IO].unsafeRunSync()
    }
  }

}
