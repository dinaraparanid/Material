import cats.syntax.all.*
import cats.{Eval, Foldable, Later}

import org.scalatest.funsuite.AnyFunSuite

import java.util
import java.util.concurrent.Future

import scala.annotation.tailrec
import scala.language.postfixOps

class Foldables extends AnyFunSuite:
  import Applicatives.applicativeFuture
  import Foldables.given
  import Monoids.given

  private def listSample: util.List[String] =
    util.List.of("1", "2", "3", "4")

  private def listOptSample: util.List[util.Optional[String]] =
    util.List.of(
      util.Optional.of("1"),
      util.Optional.empty,
      util.Optional.of("3"),
      util.Optional.empty,
      util.Optional.of("5")
    )

  private def foldLeftTest: String =
    listSample.foldLeft("")((a, b) ⇒ s"$a$b")

  private def foldRightTest: Eval[String] =
    listSample
      .foldRight(Later("")):
        (a, b) ⇒ Later(s"$a${b.value}")

  private def traverseTest: Future[Unit] =
    listSample traverse_ (Integer.parseInt >>> applicativeFuture.pure)

  test("foldLeftTest"):
    assert(foldLeftTest == "1234")

  test("foldRightTest"):
    assert(foldRightTest.value == "1234")

  test("foldTest"):
    assert(listSample.fold == "1234")
    assert(listOptSample.fold == util.Optional.of("135"))

  test("foldKTest"):
    assert(listOptSample.foldK == util.Optional.of("5"))

  test("foldMapTest"):
    assert((listSample foldMap util.Optional.of) == util.Optional.of("1234"))

  test("findTest"):
    assert((listSample find (_.toInt % 2 == 0)) == Option("2"))
    assert((listSample findM (_.toIntOption map (_ % 2 == 0)) flatten) == Option("2"))

  test("exists_forall_Test"):
    assert(listSample exists (_.toInt % 2 == 0))
    assert(listSample existsM (_.toIntOption map (_ % 2 == 0)) get)

    assert(listSample forall (_.toIntOption.isDefined))
    assert(listSample forallM (_.toIntOption map (_ ⇒ true)) get)

  test("filterTest"):
    assert((listSample filter_ (_.toInt % 2 == 0)) == List("2", "4"))

  test("traverseTest"):
    assert(traverseTest.get == ())

object Foldables:
  given listFoldable: Foldable[util.List] with
    override def foldLeft[A, B](fa: util.List[A], b: B)(f: (B, A) ⇒ B): B =
      @tailrec
      def impl(fs: util.List[A] = fa, res: B = b): B =
        if fs.isEmpty then res
        else impl(fs.subList(1, fs.size), f(res, fs.get(0)))

      impl()

    override def foldRight[A, B](
      fa: util.List[A],
      lb: Eval[B]
    )(f: (A, Eval[B]) ⇒ Eval[B]): Eval[B] =
      @tailrec
      def impl(fs: util.List[A] = fa, res: Eval[B] = lb): Eval[B] =
        if fs.isEmpty then res
        else impl(fs.subList(0, fs.size - 1), f(fs.get(fs.size - 1), res))

      impl()
