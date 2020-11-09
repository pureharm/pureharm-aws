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
