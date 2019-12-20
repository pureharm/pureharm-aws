package busymachines.pureharm.aws.sns

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 19 Nov 2019
  *
  */
trait SNSMessageEncoder[T] {
  def encode(t: T): String
}

object SNSMessageEncoder {

  def apply[T](implicit enc: SNSMessageEncoder[T]): SNSMessageEncoder[T] = enc

  //TODO: maybe add implicit conversion from a circe JSON encoder, but that would mean we depend on circe directly.
}
