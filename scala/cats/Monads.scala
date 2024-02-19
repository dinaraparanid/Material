import cats.Monad
import cats.syntax.all.*

import org.scalatest.funsuite.AnyFunSuite

import java.util
import java.util.concurrent.Future

import scala.annotation.tailrec

class Monads extends AnyFunSuite:
  import Monads.given

  private def sampleTask(using monad: Monad[Future]): Future[String] =
    monad pure:
      Thread sleep 1000
      "Я манул Жора!"

  private def sampleFalseTask(using monad: Monad[Future]): Future[String] =
    monad pure:
      Thread sleep 1000
      "Я манул Васян!"

  private def boolTask(b: Boolean)(using monad: Monad[Future]): Future[Boolean] =
    monad pure b

  private def flatMapTest(using monad: Monad[Future]): Future[String] =
    sampleTask flatMap (s ⇒ monad pure s"$s Я ем пельмени!")

  test("flatMapTest"):
    assert(flatMapTest.get() == "Я манул Жора! Я ем пельмени!")

  test("ifFTest"):
    assert:
      Monad[Future].ifM(boolTask(true))(
        ifTrue = sampleTask,
        ifFalse = sampleFalseTask
      ).get() == "Я манул Жора!"

    assert:
      Monad[Future].ifM(boolTask(false))(
        ifTrue = sampleTask,
        ifFalse = sampleFalseTask
      ).get() == "Я манул Васян!"

object Monads:
  import Applicatives.given_Applicative_Future
  import Applies.flatMapImpl

  given Monad[Future] with
    override def flatMap[A, B](fa: Future[A])(f: A ⇒ Future[B]): Future[B] =
      fa flatMapImpl f

    override def pure[A](x: A): Future[A] =
      given_Applicative_Future.pure(x)

    override def tailRecM[A, B](a: A)(f: A ⇒ Future[Either[A, B]]): Future[B] =
      @tailrec
      def impl(ab: Future[Either[A, B]] = f(a)): Future[B] =
        ab.get() match
          case Left(value)  ⇒ impl(f(value))
          case Right(value) ⇒ pure(value)

      impl()
