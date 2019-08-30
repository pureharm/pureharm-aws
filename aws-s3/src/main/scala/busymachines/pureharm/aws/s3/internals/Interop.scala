package busymachines.pureharm.aws.s3.internals

import busymachines.pureharm.effects.Async

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10 Jul 2019
  *
  */
private[s3] object Interop {
  type JCFuture[T] = java.util.concurrent.CompletableFuture[T]

  def toF[F[_]: Async, A](fa: F[JCFuture[A]]): F[A] = {
    monix.catnap.FutureLift[F, JCFuture].apply(fa)
  }
}
