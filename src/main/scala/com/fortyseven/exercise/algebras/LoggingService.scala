package com.fortyseven.exercise.algebras

trait LoggingService[F[_]] {
  def debug(message: String): F[Unit]

  def error(message: String): F[Unit]

  def info(message: String): F[Unit]

  def trace(message: String): F[Unit]

  def warn(message: String): F[Unit]
}

object LoggingService {
  def apply[F[_]](implicit ev: LoggingService[F]): LoggingService[F] = ev
}
