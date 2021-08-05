/*
 * Copyright 2019 BusyMachines
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package busymachines.pureharm.aws.sns

import busymachines.pureharm.aws.sns._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model._

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 29
  *   Nov 2019
  */
final class SNSMobilePushNotificationsOps[F[_]](
  private val jSNSClient:      SnsClient
)(implicit
  private val F:               Sync[F],
  private val blockingShifter: BlockingShifter[F],
) {

  def healthcheckSNSEndpoint(
    deviceToken: SNSDeviceToken,
    endpointARN: SNSEndpointARN,
  ): F[SNSPlatformEndpointHealthcheck] = {
    val req = GetEndpointAttributesRequest
      .builder()
      .endpointArn(endpointARN)
      .build()

    blockingShifter
      .blockOn(F.delay(jSNSClient.getEndpointAttributes(req)))
      .flatMap(healthcheckResponseParser(deviceToken))
      .recoverWith(healthcheckErrorParser)
  }

  def createEndpointARNForDeviceToken(
    platformApplicationARN: SNSPlatformApplicationARN,
    deviceToken:            SNSDeviceToken,
  ): F[SNSEndpointARN] = {
    val cpeReq = CreatePlatformEndpointRequest
      .builder()
      .platformApplicationArn(platformApplicationARN)
      .token(deviceToken)
      .build()

    blockingShifter
      .blockOn(F.delay(jSNSClient.createPlatformEndpoint(cpeReq)))
      .flatMap(createEndpointARNPublishResponseParser)
      .recoverWith(createEndpointARNErrorParser)
  }

  def pushMessage[Message: SNSMessageEncoder](
    endpointARN: SNSEndpointARN,
    message:     Message,
  ): F[Unit] = {
    val request = PublishRequest
      .builder()
      .targetArn(endpointARN)
      .message(SNSMessageEncoder[Message].encode(message))
      .build()

    blockingShifter
      .blockOn(F.delay(jSNSClient.publish(request)))
      .void
      .recoverWith(publishErrorParser)
  }

  private def healthcheckResponseParser(
    deviceToken: SNSDeviceToken
  )(resp:        GetEndpointAttributesResponse): F[SNSPlatformEndpointHealthcheck] = {
    val exists  = Option(resp.attributes().get(SNSPlatformEndpointHealthcheck.TokenAttributeID))
      .contains(SNSDeviceToken.oldType(deviceToken))
    val enabled = Option(resp.attributes().get(SNSPlatformEndpointHealthcheck.TokenAttributeID))
      .exists(_.equalsIgnoreCase(SNSPlatformEndpointHealthcheck.TrueString))

    F.pure {
      if (exists && enabled) SNSPlatformEndpointHealthcheck.Exists
      else if (exists && !enabled)
        SNSPlatformEndpointHealthcheck.Disabled
      else SNSPlatformEndpointHealthcheck.NotFound
    }
  }

  private def healthcheckErrorParser: PartialFunction[Throwable, F[SNSPlatformEndpointHealthcheck]] = {
    case _: NotFoundException => F.pure(SNSPlatformEndpointHealthcheck.NotFound)
  }

  /** The parsing of the exception is part of the official Amazon API docs... yeah ... not cool.
    * {{{https://docs.aws.amazon.com/sns/latest/dg/mobile-platform-endpoint.html}}}
    */
  private def createEndpointARNErrorParser: PartialFunction[Throwable, F[SNSEndpointARN]] = {
    case ipe: InvalidParameterException =>
      val message = ipe.getMessage
      val matcher = endpointMatcherPattern.matcher(message)
      if (matcher.matches) {
        /*
         * Quote from above linked docs:
         *
         * The platform endpoint already exists for this token, but with
         * additional custom data that createEndpoint doesn't want to overwrite.
         * Just use the existing platform endpoint.
         */
        F.pure(SNSEndpointARN(matcher.group(1)))
      }
      else F.raiseError(ipe)
  }

  private val endpointMatcherPattern: java.util.regex.Pattern = {
    java.util.regex.Pattern.compile(".*Endpoint (arn:aws:sns[^ ]+) already exists with the same [Tt]oken.*")
  }

  private def createEndpointARNPublishResponseParser(cr: CreatePlatformEndpointResponse): F[SNSEndpointARN] =
    F.pure(SNSEndpointARN(cr.endpointArn()))

  private def publishErrorParser: PartialFunction[Throwable, F[Unit]] = PartialFunction.empty
}

object SNSMobilePushNotificationsOps {

  import software.amazon.awssdk.auth.credentials._
  import software.amazon.awssdk.services.sns._

  def resource[F[_]: Sync: BlockingShifter](
    config: SNSMobilePushConfig
  ): Resource[F, SNSMobilePushNotificationsOps[F]] = Resource.eval(this.create[F](config))

  def create[F[_]: Sync: BlockingShifter](
    config: SNSMobilePushConfig
  ): F[SNSMobilePushNotificationsOps[F]] =
    Sync[F]
      .delay {
        SnsClient
          .builder()
          .credentialsProvider(
            StaticCredentialsProvider
              .create(AwsBasicCredentials.create(config.accessKeyID, config.secretAccessKey))
          )
          .region(config.region.toSDKRegion)
          .build()
      }
      .map(jSNSClient => new SNSMobilePushNotificationsOps[F](jSNSClient))
}
