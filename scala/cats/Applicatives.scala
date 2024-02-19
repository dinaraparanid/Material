import cats.{Applicative, Apply}

import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.Future

class Applicatives extends AnyFunSuite:
  import Applicatives.given

  test("pureTest"):
    val task = Applicative[Future] pure:
      Thread.sleep(1000)
      "Я манул Жора!"

    assert(task.get() == "Я манул Жора!")

object Applicatives:
  import catz.Applies.executor

  given Applicative[Future] with
    override def pure[A](x: A): Future[A] =
      executor submit (() ⇒ x)

    override def ap[A, B](ff: Future[A ⇒ B])(fa: Future[A]): Future[B] =
      Apply[Future].ap(ff)(fa)
