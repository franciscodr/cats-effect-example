package com.fortyseven.exercise.algebras

import com.fortyseven.exercise.model.Post

trait PostService[F[_]] {
  def add(post: Post): F[Post]

  def all: F[List[Post]]

  def getById(id: Long): F[Option[Post]]

  def getByUser(userId: Long): F[List[Post]]
}

object PostService {
  def apply[F[_]](implicit ev: PostService[F]): PostService[F] = ev
}
