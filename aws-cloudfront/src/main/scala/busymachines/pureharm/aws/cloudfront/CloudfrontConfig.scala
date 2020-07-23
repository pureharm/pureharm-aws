package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.config._

/**
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
sealed trait CloudfrontConfig

object CloudfrontConfig extends ConfigLoader[CloudfrontConfig] {
  import busymachines.pureharm.config.implicits._
  import busymachines.pureharm.effects.implicits._
  import pureconfig.error.CannotConvert

  final case class WithKeyFile(
    distributionDomain: CloudfrontDistributionDomain,
    privateKeyFilePath: CloudfrontPrivateKeyFilePath,
    keyPairID:          CloudfrontKeyPairID,
    urlExpirationTime:  CloudfrontURLExpiration,
  ) extends CloudfrontConfig

  final case class WithPrivateKey(
    distributionDomain: CloudfrontDistributionDomain,
    keyPairID:          CloudfrontKeyPairID,
    privateKey:         CloudfrontPrivateKey,
    privateKeyFormat:   CloudfrontPrivateKey.Format,
    urlExpirationTime:  CloudfrontURLExpiration,
  ) extends CloudfrontConfig

  //TODO: add safe phantom type support in pureconfig
  implicit protected val privateKeyReader: ConfigReader[CloudfrontPrivateKey] =
    ConfigReader[String].emap(s =>
      CloudfrontPrivateKey(s).leftMap(an =>
        CannotConvert(
          value   = s"CloudfrontPrivateKeyValue — truncated(10): ${s.take(20)}",
          toType  = "CloudfrontPrivateKey",
          because = an.toString,
        )
      )
    )

  implicit protected val formatReader: ConfigReader[CloudfrontPrivateKey.Format]  =
    semiauto.deriveEnumerationReader(transformName = n => n.toLowerCase.prepended('.'))

  implicit private val withKeyFileCFG: ConfigReader[CloudfrontConfig.WithKeyFile] =
    semiauto.deriveReader[CloudfrontConfig.WithKeyFile]

  implicit private val withPrivateKeyCFG: ConfigReader[CloudfrontConfig.WithPrivateKey] =
    semiauto.deriveReader[CloudfrontConfig.WithPrivateKey]

  implicit override val configReader: ConfigReader[CloudfrontConfig] =
    withPrivateKeyCFG.orElse(withKeyFileCFG)

  override def default[F[_]](implicit F: Sync[F]): F[CloudfrontConfig] =
    this.load("pureharm.aws.cloudfront")
}
