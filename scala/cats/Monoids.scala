import cats.*
import cats.syntax.all.*

import org.scalatest.funsuite.AnyFunSuite

import java.util

type StringMap[A] = util.Map[String, A]

class Monoids extends AnyFunSuite:
  import Monoids.given

  test("monoidTest"):
    assert:
      monoidTest == util.Map.of(
        "a", 5,
        "b", 7,
        "c", 9,
        "d", 10,
        "e", 20
      )

  test("monoidKindTest"):
    assert:
      monoidKindTest == util.Map.of(
        "a", 4,
        "b", 5,
        "c", 6,
        "d", 10,
        "e", 20
      )

  private def monoidTest: StringMap[Int] =
    val (x, y) = sampleMaps
    x |+| y

  private def monoidKindTest: StringMap[Int] =
    val (x, y) = sampleMaps
    x <+> y

object Monoids:
  given [A : Monoid]: Monoid[StringMap[A]] with
    override def empty: StringMap[A] =
      util.Map.of

    override def combine(x: StringMap[A], y: StringMap[A]): StringMap[A] =
      val res = util.HashMap[String, A](x)

      y forEach:
        (key, yv) ⇒
          Option(x get key) match
            case Some(xv) ⇒ res.put(key, xv |+| yv)
            case None     ⇒ res.put(key, yv)

      res

  given MonoidK[StringMap] with
    override def empty[A]: StringMap[A] =
      util.Map.of

    override def combineK[A](x: StringMap[A], y: StringMap[A]): StringMap[A] =
      val res = util.HashMap[String, A]()
      res putAll x
      res putAll y
      res

  given [A : Monoid]: Monoid[util.Optional[A]] with
    override def empty: util.Optional[A] =
      util.Optional.empty

    override def combine(
      x: util.Optional[A],
      y: util.Optional[A]
    ): util.Optional[A] =
      if x.isEmpty then y
      else x map (xx ⇒ y map (yy ⇒ xx |+| yy) orElseGet (() ⇒ xx))

  given MonoidK[util.Optional] with
    override def empty[A]: util.Optional[A] =
      util.Optional.empty

    override def combineK[A](
      x: util.Optional[A],
      y: util.Optional[A]
    ): util.Optional[A] =
      if x.isEmpty then y
      else x map (xx ⇒ y orElse xx)

  given [A : Monoid]: Monoid[util.List[A]] with
    override def empty: util.List[A] =
      util.List.of

    override def combine(
      x: util.List[A],
      y: util.List[A]
    ): util.List[A] =
      new util.ArrayList[A](x.size + y.size):
        addAll(x)
        addAll(y)

  given MonoidK[util.List] with
    override def empty[A]: util.List[A] =
      util.List.of

    override def combineK[A](
      x: util.List[A],
      y: util.List[A]
    ): util.List[A] =
      new util.ArrayList[A](x.size + y.size):
        addAll(x)
        addAll(y)

private def sampleMaps: (StringMap[Int], StringMap[Int]) =
  val x = util.Map.of(
    "a", 1,
    "b", 2,
    "c", 3,
    "d", 10
  )

  val y = util.Map.of(
    "a", 4,
    "b", 5,
    "c", 6,
    "e", 20
  )

  (x, y)
