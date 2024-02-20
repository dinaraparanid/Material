import cats.Comonad
import cats.data.NonEmptyList
import cats.syntax.all.*

import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.Future

class Comonads extends AnyFunSuite:
  import Applicatives.applicativeFuture
  import Comonads.given

  private def extractTest =
    applicativeFuture.map(applicativeFuture pure ()): _ ⇒
      Thread sleep 1000
      "Я манул Васян!"

  private def tasksSample: NonEmptyList[() ⇒ String] =
    NonEmptyList.of(
      () ⇒
        Thread sleep 3000
        "Я манул Васян!",
      () ⇒
        Thread sleep 3000
        "Я манул Петросян!",
      () ⇒
        Thread sleep 3000
        "Я манул Жора!",
      () ⇒
        Thread sleep 3000
        "Я манул Жека!",
      () ⇒
        Thread sleep 3000
        "Я манул Толик!",
      () ⇒
        Thread sleep 3000
        "Я манул Алкоголик!",
    )

  private def tasksAnswer: NonEmptyList[String] =
    NonEmptyList.of(
      "Я манул Васян!",
      "Я манул Петросян!",
      "Я манул Жора!",
      "Я манул Жека!",
      "Я манул Толик!",
      "Я манул Алкоголик!",
    )

  private def coflatMapTest: NonEmptyList[Future[String]] =
    applicativeFuture
      .pure(tasksSample)
      .coflatMap:
        _.get map: f ⇒
          applicativeFuture.map(applicativeFuture pure ())(_ ⇒ f())
      .get

  test("extractTest"):
    assert(extractTest.extract == "Я манул Васян!")

  test("coflatMapTest"):
    assert((coflatMapTest map (_.get)) == tasksAnswer)

object Comonads:
  import Applicatives.applicativeFuture

  given Comonad[Future] with
    override def extract[A](x: Future[A]): A = x.get

    override def coflatMap[A, B](fa: Future[A])(f: Future[A] ⇒ B): Future[B] =
      applicativeFuture.map(applicativeFuture pure ())(_ ⇒ f(fa))

    override def map[A, B](fa: Future[A])(f: A ⇒ B): Future[B] =
      applicativeFuture.map(fa)(f)
