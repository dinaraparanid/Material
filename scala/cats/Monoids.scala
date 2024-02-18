import cats.*
import cats.syntax.all.*

import org.scalatest.funsuite.AnyFunSuite

import java.util

type StringMap[A] = util.Map[String, A]

class Monoids extends AnyFunSuite:
  given [A : Monoid]: Monoid[StringMap[A]] with
    override def empty: StringMap[A] =
      util.Map.of()

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
      util.Map.of()

    override def combineK[A](x: StringMap[A], y: StringMap[A]): StringMap[A] =
      val res = util.HashMap[String, A]()
      res putAll x
      res putAll y
      res

  test("monoidTest"):
    assert:
      monoidTest() == util.Map.of(
        "a", 5,
        "b", 7,
        "c", 9,
        "d", 10,
        "e", 20
      )

  test("monoidKindTest"):
    assert:
      monoidKindTest() == util.Map.of(
        "a", 4,
        "b", 5,
        "c", 6,
        "d", 10,
        "e", 20
      )

  private def monoidTest(): StringMap[Int] =
    val (x, y) = sampleMaps
    x |+| y

  private def monoidKindTest(): StringMap[Int] =
    val (x, y) = sampleMaps
    x <+> y

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
