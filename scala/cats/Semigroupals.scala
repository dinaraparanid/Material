import cats.Semigroupal
import cats.syntax.all._

import org.scalatest.funsuite.AnyFunSuite

import java.util

class Semigroupals extends AnyFunSuite:
  given Semigroupal[util.Optional] with
    override def product[A, B](
      fa: util.Optional[A],
      fb: util.Optional[B]
    ): util.Optional[(A, B)] =
      fa flatMap (a ⇒ fb flatMap (b ⇒ util.Optional.of((a, b))))

  given Semigroupal[util.List] with
    override def product[A, B](
      fa: util.List[A],
      fb: util.List[B]
    ): util.List[(A, B)] =
      fa
        .stream()
        .flatMap: a ⇒
          fb
            .stream()
            .flatMap: b ⇒
              util.stream.Stream.of((a, b))
        .toList

  test("optionalTest"):
    val some1 = util.Optional.of("abc")
    val some2 = util.Optional.of(123)
    val none = util.Optional.empty[Int]()

    assert((some1 product some2) == util.Optional.of(("abc", 123)))
    assert((some1 product none) == none)

  test("listTest"):
    val list1 = util.List.of("a", "b", "c")
    val list2 = util.List.of(1, 2, 3)

    val combine = util.List.of(
      ("a", 1), ("a", 2), ("a", 3),
      ("b", 1), ("b", 2), ("b", 3),
      ("c", 1), ("c", 2), ("c", 3),
    )

    assert((list1 product list2) == combine)
