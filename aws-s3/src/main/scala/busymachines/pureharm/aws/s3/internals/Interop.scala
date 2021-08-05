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

package busymachines.pureharm.aws.s3.internals

import busymachines.pureharm.effects.Async

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10
  *   Jul 2019
  */
private[s3] object Interop {
  type JCFuture[T] = java.util.concurrent.CompletableFuture[T]

  def toF[F[_]: Async, A](fa: F[JCFuture[A]]): F[A] =
    Async[F].fromCompletableFuture(fa)
}
