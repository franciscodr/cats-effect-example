package com.fortyseven.exercise.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Post(id: Long, userId: Long, title: String, body: String)

object Post {
  implicit val decoder: Decoder[Post] = deriveDecoder[Post]
  implicit val encoder: Encoder[Post] = deriveEncoder[Post]
}
