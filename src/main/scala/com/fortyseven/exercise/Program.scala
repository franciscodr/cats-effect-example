package com.fortyseven.exercise

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{ActorMaterializer, Materializer}
import cats.Monad
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.fortyseven.exercise.algebras.{LoggingService, PostService}
import com.fortyseven.exercise.interpreters.{AkkaHttpPostService, TypesafeLoggingService}
import com.fortyseven.exercise.model.Post

import scala.concurrent.ExecutionContext

class Module[F[_]: Monad: LoggingService: PostService] {
  def program: F[Unit] = {
    for {
      _     <- LoggingService[F].debug("Starting our app...")
      post1 <- PostService[F].add(Post(1, 1, "My best post", "Too long"))
      post2 <- PostService[F].add(Post(2, 1, "Not my best post", "Too short"))
      posts <- PostService[F].getByUser(1)
      _     <- posts.traverse(post => LoggingService[F].info(post.title))
    } yield ()
  }
}

object Module {
  implicit def instance[F[_]: Monad: LoggingService: PostService](
      implicit system: ActorSystem,
      ec: ExecutionContext,
      materializer: Materializer): Module[F] =
    new Module[F]

  def apply[F[_]: Monad: LoggingService: PostService](implicit ev: Module[F]): Module[F] = ev
}

object Program extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val system: ActorSystem                = ActorSystem("app")
    implicit val materializer: ActorMaterializer    = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    implicit val loggingService: LoggingService[IO] = new TypesafeLoggingService[IO]

    val resource: Resource[IO, HttpExt] = Resource.make(IO.delay(Http()))(
      httpClient =>
        IO.fromFuture(
          IO.delay(httpClient.shutdownAllConnectionPools() *> system.terminate().map(_ => ()))))

    resource.allocated.flatMap {
      case (httpClient, releaseHttpClient) =>
        implicit val postService: PostService[IO] = new AkkaHttpPostService[IO](httpClient)
        Module[IO].program.attempt.flatMap(exitCode =>
          releaseHttpClient.map(_ => exitCode.fold(_ => ExitCode.Error, _ => ExitCode.Success)))
    }
  }
}
