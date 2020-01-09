package busymachines.pureharm.aws.sns

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 07 Jan 2020
  *
  */
trait LowPrioritySNSMessageEncoderImplicits {
  import busymachines.pureharm.json._
  import busymachines.pureharm.json.implicits._

  implicit def snsMessageEncoderFromCirceJSONEncoder[T: Encoder]: SNSMessageEncoder[T] =
    (t: T) => Encoder[T].apply(t).noSpacesNoNulls
}
