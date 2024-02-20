import cats.Bimonad

import java.util.concurrent.Future

object Bimonads:
  import Monads.monadFuture
  import Comonads.comonadFuture

  given Bimonad[Future] with
    override def pure[A](x: A): Future[A] =
      monadFuture pure x

    override def flatMap[A, B](fa: Future[A])(f: A ⇒ Future[B]): Future[B] =
      monadFuture.flatMap(fa)(f)

    override def tailRecM[A, B](a: A)(f: A ⇒ Future[Either[A, B]]): Future[B] =
      monadFuture.tailRecM(a)(f)

    override def coflatMap[A, B](fa: Future[A])(f: Future[A] ⇒ B): Future[B] =
      comonadFuture.coflatMap(fa)(f)

    override def extract[A](x: Future[A]): A =
      comonadFuture extract x
