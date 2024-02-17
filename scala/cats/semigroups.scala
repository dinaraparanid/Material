import cats.*
import cats.syntax.all.*

import java.util

type StringMap[A] = util.Map[String, A]

object SemigroupTest:
  given [A : Semigroup]: Semigroup[StringMap[A]] with
    override def combine(x: StringMap[A], y: StringMap[A]): StringMap[A] =
      val res = util.HashMap[String, A](x)

      y forEach:
        (key, yv) ⇒
          Option(x get key) match
            case Some(xv) ⇒ res.put(key, xv |+| yv)
            case None     ⇒ res.put(key, yv)

      res

  given SemigroupK[StringMap] with
    override def combineK[A](x: StringMap[A], y: StringMap[A]): StringMap[A] =
      val res = util.HashMap[String, A]()
      res putAll x
      res putAll y
      res

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

  def semigroupTest(): StringMap[Int] =
    val (x, y) = sampleMaps
    x |+| y

  def semigroupKindTest(): StringMap[Int] =
    val (x, y) = sampleMaps
    x <+> y

def test(): Unit =
  assert:
    SemigroupTest.semigroupTest() == util.Map.of(
      "a", 5,
      "b", 7,
      "c", 9,
      "d", 10,
      "e", 20
    )

  assert:
    SemigroupTest.semigroupKindTest() == util.Map.of(
      "a", 4,
      "b", 5,
      "c", 6,
      "d", 10,
      "e", 20
    )
