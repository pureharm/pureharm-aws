package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.phantom._

/**
  * The key has to be base64 encoded and will be checked
  */
object CloudfrontPrivateKey extends SafePhantomType[Throwable, String] {

  override def check(value: String): Either[Throwable, String] = {
    import java.nio.charset.StandardCharsets
    import java.util.Base64
    import busymachines.pureharm.effects.implicits._
    Either.catchNonFatal(new String(Base64.getDecoder.decode(value), StandardCharsets.UTF_8))
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
