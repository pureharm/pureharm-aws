package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.config._

/**
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
sealed trait CloudfrontConfig {
  def distributionDomain: CloudfrontDistributionDomain
  def keyPairID:          CloudfrontKeyPairID
  def urlExpirationTime:  CloudfrontURLExpiration
}

object CloudfrontConfig extends ConfigLoader[CloudfrontConfig] {
  import busymachines.pureharm.config.implicits._
  import busymachines.pureharm.effects.implicits._
  import pureconfig.error.CannotConvert

  final case class WithKeyFile(
    override val distributionDomain: CloudfrontDistributionDomain,
    override val keyPairID:          CloudfrontKeyPairID,
    override val urlExpirationTime:  CloudfrontURLExpiration,
    val privateKeyFilePath:          CloudfrontPrivateKeyFilePath,
  ) extends CloudfrontConfig

  final case class WithPrivateKey(
    override val distributionDomain: CloudfrontDistributionDomain,
    override val keyPairID:          CloudfrontKeyPairID,
    override val urlExpirationTime:  CloudfrontURLExpiration,
    privateKey:                      CloudfrontPrivateKey,
    privateKeyFormat:                CloudfrontPrivateKey.Format,
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
    semiauto.deriveEnumerationReader(transformName = n => s".${n.toLowerCase}")

  implicit private val withKeyFileCFG: ConfigReader[CloudfrontConfig.WithKeyFile] =
    semiauto.deriveReader[CloudfrontConfig.WithKeyFile]

  implicit private val withPrivateKeyCFG: ConfigReader[CloudfrontConfig.WithPrivateKey] =
    semiauto.deriveReader[CloudfrontConfig.WithPrivateKey]

  implicit override val configReader: ConfigReader[CloudfrontConfig] =
    withPrivateKeyCFG.orElse(withKeyFileCFG)

  override def default[F[_]](implicit F: Sync[F]): F[CloudfrontConfig] =
    this.load("pureharm.aws.cloudfront")
}
