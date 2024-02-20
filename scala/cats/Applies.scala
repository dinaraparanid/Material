import cats.Apply
import cats.syntax.all._

import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.{ExecutorService, Executors, Future}

private val MANULS: Int = 10_000_000

class Applies extends AnyFunSuite:
  import catz.Applies.given
  import catz.Applies.executor

  private def millionGenTask: Future[List[Int]] =
    executor submit { () ⇒ List.iterate(1, MANULS)(_ + 1) }

  private def manulTransformGenTask: Future[List[Int] ⇒ List[String]] =
    executor submit { () ⇒ manulListGenerator }

  private def manulWordGenTask: Future[List[String]] =
    executor submit { () ⇒ List.fill(MANULS)("манул") }

  private def uniteManulsTask: Future[(List[Int], List[String]) ⇒ List[String]] =
    executor submit { () ⇒ uniteManuls }

  private def nameManulsTask(manuls: List[String]): Future[List[String]] =
    executor submit { () ⇒ manuls }

  private def mapTest: Future[List[String]] =
    millionGenTask map (_ map manulGenerator)

  private def mapNTest: Future[List[String]] =
    (millionGenTask, manulWordGenTask) mapN uniteManuls

  private def apTest: Future[List[String]] =
    manulTransformGenTask <*> millionGenTask

  private def apNTest: Future[List[String]] =
    (millionGenTask, manulWordGenTask) apWith uniteManulsTask

  private def tupleNTest: Future[List[String]] =
    (nameManulsTask(manManuls), nameManulsTask(womanManuls))
      .tupled
      .map(pairedManuls)

  private def productTest: Future[List[String]] =
    Apply[Future]
      .product(nameManulsTask(manManuls), nameManulsTask(womanManuls))
      .map(pairedManuls)

  private def composeTest: Future[List[String]] =
    val ap = Apply[Future] compose Apply[List]
    ap.map(millionGenTask)(manulGenerator)

  private def manulsAnswer: List[String] =
    List.iterate(1, MANULS)(_ + 1) map manulGenerator

  private def manulsCouplesAnswer: List[String] =
    for
      m ← manManuls
      w ← womanManuls
    yield coupleManuls(m, w)

  test("mapTest"):
    assert(mapTest.get() == manulsAnswer)

  test("mapNTest"):
    assert(mapNTest.get() == manulsAnswer)

  test("apTest"):
    assert(apTest.get() == manulsAnswer)

  test("apNTest"):
    assert(apNTest.get() == manulsAnswer)

  test("tupleProductTest"):
    val tup = tupleNTest.get()
    val prod = productTest.get()
    val ans = manulsCouplesAnswer
    assert(tup == prod && tup == ans)

  test("composeTest"):
    assert(composeTest.get() == manulsAnswer)

object Applies:
  implicit val executor: ExecutorService =
    Executors.newCachedThreadPool()

  extension [A](future: Future[A])
    def mapImpl[B](f: A ⇒ B): Future[B] =
      executor submit (() ⇒ f(future.get()))

    def flatMapImpl[B](f: A ⇒ Future[B]): Future[B] =
      f(future.get())

  given applyFuture: Apply[Future] with
    override def ap[A, B](ff: Future[A ⇒ B])(fa: Future[A]): Future[B] =
      fa flatMapImpl (a ⇒ ff.mapImpl(f ⇒ f(a)))

    override def map[A, B](fa: Future[A])(f: A ⇒ B): Future[B] =
      fa mapImpl f

private def manulGenerator(value: Int): String =
  s"$value манул"

private def manulListGenerator(vals: List[Int]): List[String] =
  vals map manulGenerator

private def uniteManuls(xs: List[Int], manuls: List[String]): List[String] =
  (xs zip manuls) map ((x, m) ⇒ s"$x $m")

private def manManuls: List[String] =
  List("Жека", "Жорик", "Васян")

private def womanManuls: List[String] =
  List("Стелла", "Эльвира", "Габриэлла")

private def pairedManuls(ms: List[String], ws: List[String]) =
  (ms, ws).tupled map coupleManuls

private def coupleManuls(m: String, w: String) =
  s"Он $m, а она $w"
