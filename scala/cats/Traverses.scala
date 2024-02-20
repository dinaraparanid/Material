import cats.{Applicative, Eval, Traverse}
import cats.syntax.all.*

import org.scalatest.funsuite.AnyFunSuite

import java.util
import java.util.concurrent.Future

import scala.language.postfixOps

class Traverses extends AnyFunSuite:
  import Applicatives.applicativeFuture
  import Traverses.given

  def traverseTest: Future[util.List[String]] =
    util.List.of(1000, 2000, 4000) traverse: ms ⇒
      applicativeFuture pure:
        Thread sleep ms
        s"Hello after $ms ms"

  def sequenceTest: Future[util.List[String]] =
    util.List
      .of(1000, 2000, 4000)
      .map: ms ⇒
        applicativeFuture pure:
          Thread sleep ms
          s"Hello after $ms ms"
      .sequence

  def traverseAnswer: util.List[String] =
    util.List.of(
      "Hello after 1000 ms",
      "Hello after 2000 ms",
      "Hello after 4000 ms",
    )

  test("traverse_sequence_Test"):
    assert(traverseTest.get == traverseAnswer)
    assert(sequenceTest.get == traverseAnswer)

object Traverses:
  import Foldables.listFoldable

  given Traverse[util.List] with
    override def traverse[G[_] : Applicative, A, B](fa: util.List[A])(f: A ⇒ G[B]): G[util.List[B]] =
      if fa.isEmpty then Applicative[G] pure util.List.of
      else (f(fa.get(0)) product traverse(fa.subList(1, fa.size))(f)) map:
        case (elem, list) ⇒
          new util.ArrayList[B](list.size):
            add(elem)
            addAll(list)

    override def foldLeft[A, B](fa: util.List[A], b: B)(f: (B, A) ⇒ B): B =
      listFoldable.foldLeft(fa, b)(f)

    override def foldRight[A, B](fa: util.List[A], lb: Eval[B])(f: (A, Eval[B]) ⇒ Eval[B]): Eval[B] =
      listFoldable.foldRight(fa, lb)(f)
