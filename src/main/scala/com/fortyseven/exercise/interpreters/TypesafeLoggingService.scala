package com.fortyseven.exercise.interpreters

import cats.effect.Sync
import com.fortyseven.exercise.algebras.LoggingService
import com.typesafe.scalalogging.Logger

class TypesafeLoggingService[F[_]: Sync] extends LoggingService[F] {

  val logger: Logger = Logger("my-app")

  override def debug(message: String): F[Unit] = Sync[F].delay(logger.debug(message))

  override def error(message: String): F[Unit] = Sync[F].delay(logger.error(message))

  override def info(message: String): F[Unit] = Sync[F].delay(logger.info(message))

  override def trace(message: String): F[Unit] = Sync[F].delay(logger.trace(message))

  override def warn(message: String): F[Unit] = Sync[F].delay(logger.warn(message))
}
