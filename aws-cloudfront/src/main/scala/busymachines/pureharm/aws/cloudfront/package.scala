package busymachines.pureharm.aws

import busymachines.pureharm.phantom._
import cats.effect.MonadThrow

import scala.concurrent.duration.FiniteDuration

/** @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
package object cloudfront {

  object CloudfrontDistributionDomain extends SproutSub[String] with SproutShow[String]
  /** See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html AmazonCloudFront]]
    *
    * Your distribution domain usually looks like this:
    * ``a1b2c3d4e5f6g7.cloudfront.net`` — value taken from above URL.
    */
  type CloudfrontDistributionDomain = CloudfrontDistributionDomain.Type

  object CloudfrontPrivateKeyFilePath extends SproutSub[java.nio.file.Path]
  type CloudfrontPrivateKeyFilePath = CloudfrontPrivateKeyFilePath.Type

  type CloudfrontPrivateKey = CloudfrontPrivateKey.Type

  /** The key has to be base64 encoded and will be checked
    */
  object CloudfrontPrivateKey extends SproutRefinedSubThrow[String] {

    override def refine[F[_]](o: String)(implicit m: MonadThrow[F]): F[String] = {
      import java.nio.charset.StandardCharsets
      import java.util.Base64
      import busymachines.pureharm.effects.implicits._

      Either.catchNonFatal[String](new String(Base64.getDecoder.decode(o), StandardCharsets.UTF_8)).liftTo[F]
    }

    sealed trait Format

    implicit class Ops(pk: CloudfrontPrivateKey) {

      def utf8Bytes: Array[Byte] = pk.getBytes(
        java.nio.charset.StandardCharsets.UTF_8
      )
    }

    case object PEM extends Format {
      override def toString: String = ".pem"
    }

    case object DER extends Format {
      override def toString: String = ".der"
    }
  }

  object CloudfrontKeyPairID extends SproutSub[String]
  /** See [[https://support.s3mediamaestro.com/article/204-how-to-obtain-your-cloudfront-key-pair-id-and-private-key]]
    * on what this value is.
    */
  type CloudfrontKeyPairID = CloudfrontKeyPairID.Type

  object CloudfrontURLExpiration extends SproutSub[FiniteDuration]
  type CloudfrontURLExpiration = CloudfrontURLExpiration.Type

  object CloudfrontSignedURL extends SproutSub[String]
  /** This type represents a URL fully accessible by an end user,
    * configured with a timeout. Once you construct a value of this type
    * no additional modifications are required, and it can be safely passed
    * along to the client as is.
    *
    * See [[https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/CFPrivateDistJavaDevelopment.html AmazonCloudFront]]
    *
    * The idea is that this domain serves as the base prefix to which we add
    * the [[busymachines.pureharm.aws.s3.S3FileKey]] to uniquely identify this
    * resource, which we then sign, to yield us a final value.
    *
    * It's a rather annoying API on part of amazon, because it's the client's responsibility
    * to correctly construct this URL from these two parts, instead of them providing an
    * API where you pass along these two parts... anyway.
    */
  type CloudfrontSignedURL = CloudfrontSignedURL.Type
}
