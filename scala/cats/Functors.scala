import cats.Functor
import org.scalatest.funsuite.AnyFunSuite

import java.util.function.Supplier

import scala.annotation.tailrec
import scala.util.Random

class Functors extends AnyFunSuite:
  import Functors.given

  def fmapTest(using f: Functor[Supplier]): Supplier[String] =
    f.fmap(randomInt)(manulGenerator)

  def fproductTest(using f: Functor[Supplier]): Supplier[(Float, Float)] =
    f.fproduct(randomProb)(x ⇒ 1 - x)

  def liftTest(value: Int)(using f: Functor[Supplier]): Supplier[Option[Int]] =
    f.lift(factorial)(requiredInt(value))

  def composeTest(value: Int)(using f: Functor[Supplier]): Supplier[List[String]] =
    val fun = Functor[Supplier] compose Functor[List]
    val repeater = f.fmap(requiredInt(value))(iota)
    fun.map(repeater)(manulGenerator)

  def ifFAnswer: List[String] =
    List(
      "Я манул Жора!",
      "Я манул Васян!",
      "Я манул Жора!",
      "Я манул Жора!",
      "Я манул Васян!",
    )

  def ifFTest: Supplier[List[String]] =
    (Functor[Supplier] compose Functor[List])
      .ifF(trueFalseSup)(
        ifTrue = "Я манул Жора!",
        ifFalse = "Я манул Васян!"
      )

  test("fmapTest"):
    assert (fmapTest.get() matches "\\d манул")

  test("fproductTest"):
    val (x, y) = fproductTest.get()
    assert(x + y == 1F)

  test("liftTest"):
    assert(liftTest(value = -1).get().isEmpty)
    assert(liftTest(value = 5).get() == Option(120))

  test("composeTest"):
    assert(composeTest(value = 3).get() == List("1 манул", "2 манул", "3 манул"))

  test("ifFTest"):
    assert(ifFTest.get() == ifFAnswer)

object Functors:
  given Functor[Supplier] with
    override def map[A, B](fa: Supplier[A])(f: A ⇒ B): Supplier[B] =
      () => f(fa.get())

private def randomInt =
  new Supplier[Int]:
    override def get(): Int =
      Random().between(0, 10)

private def requiredInt(int: Int) =
  new Supplier[Int]:
    override def get(): Int = int

private def randomProb =
  new Supplier[Float]:
    override def get(): Float =
      Random().between(0, 1)

private def trueFalseSup =
  new Supplier[List[Boolean]]:
    override def get(): List[Boolean] =
      trueFalseList

private def factorial(value: Int): Option[Int] =
  @tailrec
  def impl(x: Int = value, res: Int = 1): Int =
    if x == 0 then res else impl(x - 1, res * x)

  if value < 0 then Option.empty else Option(impl(value))

private def manulGenerator(value: Int): String =
  s"$value манул"

private def iota(value: Int): List[Int] =
  List.iterate(1, value)(_ + 1)

private def trueFalseList: List[Boolean] =
  List(true, false, true, true, false)
