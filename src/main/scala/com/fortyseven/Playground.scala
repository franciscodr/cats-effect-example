package com.fortyseven

import cats.kernel.Monoid
import cats.implicits._

case class Invoice(amount: Double)

object Invoice {
  implicit val instance: Monoid[Invoice] = new Monoid[Invoice] {
    override def empty: Invoice = Invoice(0)

    override def combine(x: Invoice, y: Invoice): Invoice = Invoice(x.amount + y.amount)
  }
}

object Playground extends App {

  def sumInt(list: List[Int]): Int = list.foldLeft(0)(_ + _)

  def sumLong(list: List[Long]): Long = list.foldLeft(0L)(_ + _)

  def appendStrings(list: List[String]): String = list.foldLeft("")(_ + _)

  def sum[A: Monoid](list: List[A]): A = list.foldLeft(Monoid[A].empty)(Monoid[A].combine(_, _))

  println(sum(List(1, 2, 3)))
  println(sum(List("a", "b", "c")))

  println(sum(List(Invoice(2), Invoice(50), Invoice(20))))
}
