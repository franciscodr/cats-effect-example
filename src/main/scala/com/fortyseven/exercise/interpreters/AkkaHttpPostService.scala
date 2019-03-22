package com.fortyseven.exercise.interpreters

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import cats.effect.Async
import com.fortyseven.exercise.algebras.PostService
import com.fortyseven.exercise.model.Post
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.Decoder

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AkkaHttpPostService[F[_]: Async](httpClient: HttpExt)(
    implicit system: ActorSystem,
    ec: ExecutionContext,
    materializer: Materializer)
    extends PostService[F] {

  def addWithFuture(post: Post): Future[Post] =
    for {
      requestEntity <- Marshal(post).to[RequestEntity]
      httpResponse <- httpClient.singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = Uri("https://jsonplaceholder.typicode.com/posts"),
          entity = requestEntity
        )
      )
      result <- Unmarshal(httpResponse.entity).to[Post]
    } yield result

  def queryWithFuture[A: Decoder](uri: Uri): Future[A] =
    for {
      httpResponse <- httpClient.singleRequest(
        HttpRequest(
          method = HttpMethods.GET,
          uri = uri
        )
      )
      result <- Unmarshal(httpResponse.entity).to[A]
    } yield result

  override def add(post: Post): F[Post] =
    Async[F].async(cb =>
      addWithFuture(post).onComplete {
        case Success(value)     => cb(Right(value))
        case Failure(exception) => cb(Left(exception))
    })

  override def all: F[List[Post]] =
    Async[F].async(cb =>
      queryWithFuture[List[Post]](Uri("https://jsonplaceholder.typicode.com/posts")).onComplete {
        case Success(value)     => cb(Right(value))
        case Failure(exception) => cb(Left(exception))
    })

  override def getById(id: Long): F[Option[Post]] =
    Async[F].async(
      cb =>
        queryWithFuture[Option[Post]](Uri(s"https://jsonplaceholder.typicode.com/posts/$id"))
          .onComplete {
            case Success(value)     => cb(Right(value))
            case Failure(exception) => cb(Left(exception))
        })

  override def getByUser(userId: Long): F[List[Post]] =
    Async[F].async(
      cb =>
        queryWithFuture[List[Post]](Uri("https://jsonplaceholder.typicode.com/posts").withQuery(
          Uri.Query(("userId", userId.toString)))).onComplete {
          case Success(value)     => cb(Right(value))
          case Failure(exception) => cb(Left(exception))
      })
}
