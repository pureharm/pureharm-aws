package busymachines.pureharm.aws.cloudfront

import busymachines.pureharm.effects._
import busymachines.pureharm.config._

/**
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  */
final case class CloudfrontConfig(
  distributionDomain: CloudfrontDistributionDomain,
  privateKeyFilePath: CloudfrontPrivateKeyFilePath,
  keyPairID:          CloudfrontKeyPairID,
  urlExpirationTime:  CloudfrontURLExpiration,
)

object CloudfrontConfig extends ConfigLoader[CloudfrontConfig] {
  import busymachines.pureharm.config.implicits._

  implicit override def configReader: ConfigReader[CloudfrontConfig] = semiauto.deriveReader[CloudfrontConfig]
  override def default[F[_]](implicit F: Sync[F]): F[CloudfrontConfig] = this.load("pureharm.aws.cloudfront")
}
