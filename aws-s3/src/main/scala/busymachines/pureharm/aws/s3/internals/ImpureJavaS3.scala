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

package busymachines.pureharm.aws.s3.internals

import busymachines.pureharm.aws.core.AmazonRegion
import busymachines.pureharm.aws.s3._
import busymachines.pureharm.effects._
import busymachines.pureharm.effects.implicits._

/** @author
  *   Lorand Szakacs, https://github.com/lorandszakacs
  * @since 10
  *   Jul 2019
  */
object ImpureJavaS3 {

  import software.amazon.awssdk.core.internal.util.Mimetype
  import software.amazon.awssdk.services.s3.S3AsyncClient
  import software.amazon.awssdk.core.ResponseBytes
  import software.amazon.awssdk.core.async._
  import software.amazon.awssdk.services.s3.model._

  def put[F[_]](
    client:  S3AsyncClient
  )(
    bucket:  S3Bucket,
    key:     S3FileKey,
    content: S3BinaryContent,
  )(implicit
    F:       Async[F]
  ): F[S3FileKey] =
    for {
      jpath <- S3FileKey.asJPath[F](key)
      mimeType   = Mimetype.getInstance().getMimetype(jpath)
      putRequest =
        PutObjectRequest
          .builder()
          .bucket(bucket)
          .key(key)
          .contentType(mimeType)
          .build()

      reqBody <- Sync[F].delay(AsyncRequestBody.fromBytes(content))
      ff: F[Interop.JCFuture[PutObjectResponse]] = Sync[F].delay(
        client.putObject(putRequest, reqBody)
      )
      _ <- Interop.toF(ff)
    } yield key

  def putStream[F[_]](
    client:  S3AsyncClient
  )(
    bucket:  S3Bucket,
    key:     S3FileKey,
    content: S3BinaryStream[F],
  )(implicit
    F:       ConcurrentEffect[F]
  ): F[S3FileKey] =
    for {
      jpath <- S3FileKey.asJPath[F](key)
      mimeType   = Mimetype.getInstance().getMimetype(jpath)
      putRequest =
        PutObjectRequest
          .builder()
          .bucket(bucket)
          .key(key)
          .contentType(mimeType)
          .build()

      //unfortunately we cannot use reactive streams publisher because
      //then we have to supply the length... so it still means compiling the stream...
      bytes: Array[Byte] <- {
        content.chunks
          .map(_.toArray)
          .compile
          .toList
          .map(lob => lob.foldRight(Array.empty[Byte])((e, acc) => e ++ acc))
      }

      reqBody <- Sync[F].delay(AsyncRequestBody.fromBytes(bytes))

      ff: F[Interop.JCFuture[PutObjectResponse]] = Sync[F].delay(
        client.putObject(putRequest, reqBody)
      )
      _ <- Interop.toF(ff)
    } yield key

  def get[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    key:    S3FileKey,
  ): F[S3BinaryContent] =
    for {
      transformer <- asyncBytesTransformer[F]
      getReq      <- GetObjectRequest.builder().bucket(bucket).key(key).bucket(bucket).build().pure[F]
      content     <- Interop.toF(Sync[F].delay(client.getObject(getReq, transformer)))
    } yield content

  def getMetadata[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    key:    S3FileKey,
  ): F[S3Metadata] =
    for {
      getReq  <- HeadObjectRequest.builder().bucket(bucket).key(key).bucket(bucket).build().pure[F]
      content <- Interop.toF(Sync[F].delay(client.headObject(getReq)))
    } yield S3Metadata(
      contentLength = S3ContentLengthBytes(content.contentLength()),
      javaMetadata  = content,
    )

  def getStream[F[_]: Async: BlockingShifter](client: S3AsyncClient)(
    bucket:    S3Bucket,
    key:       S3FileKey,
    chunkSize: Int,
  ): F[S3BinaryStream[F]] =
    for {
      transformer <- streamTransformer[F](chunkSize)
      getReq      <- GetObjectRequest.builder().bucket(bucket).key(key).bucket(bucket).build().pure[F]
      content     <- Interop.toF(Sync[F].delay(client.getObject(getReq, transformer)))
    } yield content

  def delete[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    key:    S3FileKey,
  ): F[Unit] =
    for {
      delReq <- DeleteObjectRequest.builder().bucket(bucket).key(key).bucket(bucket).build().pure[F]
      _      <- Interop.toF(Sync[F].delay(client.deleteObject(delReq)))
    } yield ()

  def list[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    prefix: S3Path,
  ): F[List[S3FileKey]] = {
    import scala.jdk.CollectionConverters._
    for {
      listReq <- ListObjectsRequest.builder().bucket(bucket).prefix(prefix).build().pure[F]
      keys    <-
        Interop
          .toF(Sync[F].delay(client.listObjects(listReq)))
          .flatMap(_.contents().asScala.toList.traverse(obj => S3FileKey[F](obj.key())))
    } yield keys
  }

  def exists[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    key:    S3FileKey,
  ): F[Boolean] =
    for {
      headReq <- HeadObjectRequest.builder().bucket(bucket).key(key).build().pure[F]
      exists  <- Interop.toF(Sync[F].delay(client.headObject(headReq))).map(_ => true).recover { case _: S3Exception =>
        false
      }
    } yield exists

  def copy[F[_]: Async](client: S3AsyncClient)(
    fromBucket: S3Bucket,
    fromKey:    S3FileKey,
    toBucket:   S3Bucket,
    toKey:      S3FileKey,
  ): F[Unit] = {
    import java.net.URLEncoder
    import java.nio.charset.StandardCharsets
    for {
      urlEncodedSource <- Sync[F].delay(URLEncoder.encode(s"$fromBucket/$fromKey", StandardCharsets.UTF_8.toString))
      copyReq          <-
        CopyObjectRequest
          .builder()
          .copySource(urlEncodedSource)
          .destinationBucket(toBucket)
          .destinationKey(toKey)
          .build()
          .pure[F]
      _                <- Interop.toF(Sync[F].delay(client.copyObject(copyReq)))
    } yield ()
  }

  def listBuckets[F[_]: Async](client: S3AsyncClient): F[List[S3Bucket]] = {
    val req = ListBucketsRequest
      .builder()
      .build()

    Interop.toF(Sync[F].delay(client.listBuckets(req))).map { resp =>
      import scala.jdk.CollectionConverters._
      resp.buckets().asScala.toList.map(b => S3Bucket(b.name()))
    }
  }

  def createBucket[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket,
    region: AmazonRegion,
  ): F[Unit] = {
    val req = CreateBucketRequest
      .builder()
      .bucket(bucket)
      .createBucketConfiguration(
        CreateBucketConfiguration
          .builder()
          .locationConstraint(region)
          .build()
      )
      .build()

    Interop.toF(Sync[F].delay(client.createBucket(req))).void
  }

  def deleteBucket[F[_]: Async](client: S3AsyncClient)(
    bucket: S3Bucket
  ): F[Unit] = {
    val req = DeleteBucketRequest
      .builder()
      .bucket(bucket)
      .build()

    Interop.toF(Sync[F].delay(client.deleteBucket(req))).void
  }

  //bytes transformer

  private def asyncBytesTransformer[F[_]: Sync]: F[AsyncResponseTransformer[GetObjectResponse, S3BinaryContent]] =
    for {
      bf <- Sync[F].delay(AsyncResponseTransformer.toBytes[GetObjectResponse])
    } yield new AsyncBytesTransformer(bf)

  private class AsyncBytesTransformer(
    private val impl: AsyncResponseTransformer[GetObjectResponse, ResponseBytes[GetObjectResponse]]
  ) extends AsyncResponseTransformer[GetObjectResponse, S3BinaryContent] {
    import java.nio.ByteBuffer
    import java.util.concurrent.CompletableFuture

    override def prepare(): CompletableFuture[S3BinaryContent] =
      impl.prepare().thenApply(cf => S3BinaryContent(cf.asByteArray()))

    override def onResponse(response: GetObjectResponse): Unit = impl.onResponse(response)

    override def onStream(publisher: SdkPublisher[ByteBuffer]): Unit = impl.onStream(publisher)

    override def exceptionOccurred(error: Throwable): Unit = impl.exceptionOccurred(error)
  }

  private def streamTransformer[F[_]: Sync: BlockingShifter](
    chunkSize: Int
  ): F[AsyncResponseTransformer[GetObjectResponse, S3BinaryStream[F]]] =
    for {
      bf <- Sync[F].delay(AsyncResponseTransformer.toBytes[GetObjectResponse])
    } yield new StreamTransformer[F](chunkSize, bf)

  private class StreamTransformer[F[_]: Sync: BlockingShifter](
    val chunkSize:    Int,
    private val impl: AsyncResponseTransformer[GetObjectResponse, ResponseBytes[GetObjectResponse]],
  ) extends AsyncResponseTransformer[GetObjectResponse, S3BinaryStream[F]] {
    import java.nio.ByteBuffer
    import java.util.concurrent.CompletableFuture

    override def prepare(): CompletableFuture[S3BinaryStream[F]] =
      impl.prepare().thenApply { cf =>
        //
        import fs2.io
        val inputStream = Sync[F].delay(cf.asInputStream())
        implicit val cs: ContextShift[F] = BlockingShifter[F].contextShift
        io.readInputStream(fis = inputStream, chunkSize = chunkSize, BlockingShifter[F].blocker, closeAfterUse = false)
      }

    override def onResponse(response: GetObjectResponse): Unit = impl.onResponse(response)

    override def onStream(publisher: SdkPublisher[ByteBuffer]): Unit = impl.onStream(publisher)

    override def exceptionOccurred(error: Throwable): Unit = impl.exceptionOccurred(error)
  }

}
