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
