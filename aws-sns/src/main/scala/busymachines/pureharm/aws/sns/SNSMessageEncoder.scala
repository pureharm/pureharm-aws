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
  * @since 19 Nov 2019
  */
trait SNSMessageEncoder[T] {
  def encode(t: T): String
}

object SNSMessageEncoder extends LowPrioritySNSMessageEncoderImplicits {
  def apply[T](implicit enc: SNSMessageEncoder[T]): SNSMessageEncoder[T] = enc

  implicit object SNSStringMessageEncoder extends SNSMessageEncoder[String] {
    override def encode(t: String): String = t
  }
}
