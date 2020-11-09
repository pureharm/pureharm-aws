package busymachines.pureharm.aws.s3

import busymachines.pureharm.phantom._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

/** @author Daniel Incicau
  * @since 03/12/2019
  */
object S3Path extends SafePhantomType[Throwable, String] {
  import java.nio.file._

  def apply(fst: String, snd: String, rest: String*): Attempt[this.Type] =
    this.apply(s"$fst/$snd/${rest.mkString("", "/", "")}")

  override def check(value: String): Attempt[String] =
    validatePath(value).map(_.normalize().toString)

  final def asJPath(s3FilePath: this.Type): Attempt[Path] =
    Attempt.catchNonFatal(Paths.get(this.despook(s3FilePath))).adaptError { case NonFatal(e) =>
      InvalidPathIA(s3FilePath, e)
    }

  final def asJPathUnsafe(s3FilePath: this.Type): Path =
    asJPath(s3FilePath).unsafeGet()

  private def validatePath(path: String): Attempt[Path] =
    Attempt.catchNonFatal(Paths.get(path)).adaptError { case NonFatal(e) => InvalidPathIA(path, e) }
}
