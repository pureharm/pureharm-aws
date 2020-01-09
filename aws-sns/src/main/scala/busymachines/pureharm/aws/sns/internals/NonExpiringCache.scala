package busymachines.pureharm.aws.sns.internals

import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Nov 2019
  *
  */
trait NonExpiringCache[F[_], K, V] {
  def put(k: K, v: V): F[Unit]

  def putIfAbsent(k: K, fv: F[V]): F[Unit]

  def get(k: K): F[Option[V]]

  def getOrPutIfAbsent(k: K, fv: F[V]): F[V]

  def replace(k: K, fv: F[V]): F[V]

  def evict(k: K): F[Unit]

  def filter(p: ((K, V)) => Boolean): F[Map[K, V]]
}

object NonExpiringCache {

  def apply[F[_]: Concurrent, K, V]: F[NonExpiringCache[F, K, V]] = {
    for {
      ref <- Ref.of[F, Map[K, V]](Map.empty[K, V])
      sem <- Semaphore(1)
    } yield new NonExpiringCacheImpl[F, K, V](ref, sem)
  }

  private class NonExpiringCacheImpl[F[_], K, V](
    private[this] val m:   Ref[F, Map[K, V]],
    private[this] val sem: Semaphore[F],
  )(
    implicit private val F: Concurrent[F],
  ) extends NonExpiringCache[F, K, V] {

    override def put(k: K, v: V): F[Unit] = {
      val acquire = sem.acquire
      val use     = m.update(old => old + (k -> v))
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    override def putIfAbsent(k: K, fv: F[V]): F[Unit] = {
      val acquire = sem.acquire
      val use = for {
        currentMap <- m.get
        _ <- currentMap.contains(k).ifFalseRun {
          fv.flatMap(newValue => m.update(old => old + (k -> newValue)))
        }
      } yield ()
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    override def get(k: K): F[Option[V]] = {
      val acquire = sem.acquire
      val use     = m.get.map(_.get(k))
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    override def getOrPutIfAbsent(k: K, fv: F[V]): F[V] = {
      val acquire = sem.acquire
      val use = for {
        currentMap <- m.get
        value <- currentMap.get(k) match {
          case None    => fv.flatMap(newValue => m.update(old => old + (k -> newValue)).as(newValue))
          case Some(v) => F.pure(v)
        }
      } yield value
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    def replace(k: K, fv: F[V]): F[V] = {
      val acquire = sem.acquire
      val use = for {
        currentMap <- m.get
        value <- currentMap.get(k) match {
          case None    => fv.flatMap(newValue => m.update(old => old.updated(k, newValue)).as(newValue))
          case Some(v) => F.pure(v)
        }
      } yield value
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    override def evict(k: K): F[Unit] = {
      val acquire = sem.acquire
      val use     = m.update(old => old.-(k))
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }

    override def filter(p: ((K, V)) => Boolean): F[Map[K, V]] = {
      val acquire = sem.acquire
      val use     = m.get.map(all => all.filter(p))
      val release = sem.release
      F.bracket(acquire)(_ => use)(_ => release)
    }
  }

}
