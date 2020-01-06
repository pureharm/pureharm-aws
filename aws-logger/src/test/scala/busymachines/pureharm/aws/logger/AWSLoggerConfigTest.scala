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

import busymachines.pureharm.config.ConfigAggregateAnomalies
import busymachines.pureharm.effects._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 14 May 2019
  *
  */
final class AWSLoggerConfigTest extends AnyFunSuite with Matchers {

  test("... read config from 'production' reference.conf") {
    noException shouldBe thrownBy {
      AWSLoggerConfig.default[IO].unsafeRunSync()
    }
  }

  test("... test-1 — read fully formed value — enabled = true") {
    noException shouldBe thrownBy {
      val c = AWSLoggerConfig.fromNamespace[IO]("test-config.test-1.pureharm.aws.logger").unsafeRunSync()
      assert(c.enabled)
      assert(c.cloudwatch.isDefined)
    }
  }

  test("... test-2 — fully formed value — enabled = false") {
    noException shouldBe thrownBy {
      val c = AWSLoggerConfig.fromNamespace[IO]("test-config.test-2.pureharm.aws.logger").unsafeRunSync()
      assert(!c.enabled)
      assert(c.cloudwatch.isEmpty)
    }
  }

  test("... test-3 — just enabled = false") {
    noException shouldBe thrownBy {
      val c = AWSLoggerConfig.fromNamespace[IO]("test-config.test-3.pureharm.aws.logger").unsafeRunSync()
      assert(!c.enabled)
      assert(c.cloudwatch.isEmpty)
    }
  }

  test("... test-4 — invalid amazon region — enabled = false") {
    val exp = the[ConfigAggregateAnomalies] thrownBy {
      AWSLoggerConfig.fromNamespace[IO]("test-config.test-4.pureharm.aws.logger").unsafeRunSync()
    }
    assert(exp.getLocalizedMessage.contains("Invalid Amazon region"))
  }

}
