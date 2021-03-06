package chapter5

sealed trait Stream[+A] {
  def foldRight[B](z: => B)(f: (A, =>B) => B): B = this match {
    case Cons(h,t) => f(h(), t().foldRight(z)(f))
    case _ => z
  }

  def map[B](f: A => B): Stream[B] =
    foldRight[Stream[B]](Stream.empty)((hd, tl) => Stream.cons(f(hd), tl))

  def flatMap[B](f: A => Stream[B]): Stream[B] =
    foldRight[Stream[B]](Stream.empty)((hd, tl) => f(hd) match {
      case Empty => tl
      case Cons(h, _) => Stream.cons(h(), tl)
    })

  def filter(p: A => Boolean): Stream[A] =
    foldRight[Stream[A]](Stream.empty)((hd, tl) => if (p(hd)) Stream.cons(hd, tl.filter(p)) else tl.filter(p))

  def append[B >: A](that: Stream[B]): Stream[B] =
    foldRight[Stream[B]](that)((hd, tl) => Stream.cons(hd, tl))

  def headOption: Option[A] =
    foldRight[Option[A]](None)((hd, tl) => Some(hd))


  def toList: List[A] = this match {
    case Empty => Nil
    case Cons(h, t) => h() :: t().toList
  }

  def take(n: Int): Stream[A] =
    if (n == 0) Stream.empty else this match {
      case Empty => Stream.empty
      case Cons(h, t) => Stream.cons(h(), t().take(n-1))
    }

  def drop(n: Int): Stream[A] =
    if (n == 0) this else this match {
      case Empty => Stream.empty
      case Cons(h, t) => t().drop(n-1)
    }

  def takeWhile(p: A => Boolean): Stream[A] =
    foldRight[Stream[A]](Stream.empty)((hd,tl) => if (p(hd)) Stream.cons(hd, tl.takeWhile(p)) else Stream.empty)

  def forAll(p: A => Boolean): Boolean = this match {
    case Empty => true
    case Cons(h,t) if !p(h()) => false
    case Cons(h,t) => t().forAll(p)
  }
}

case object Empty extends Stream[Nothing]
case class Cons[A](h: () => A, t: () => Stream[A]) extends Stream[A]

object Stream {
  def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
    lazy val head = hd
    lazy val tail = tl

    Cons(() => head, () => tail)
  }

  def empty[A]: Stream[A] = Empty

  def apply[A](as: A*): Stream[A] =
    if (as.isEmpty) empty else cons(as.head, apply(as.tail:_*))

  def constant[A](a: A): Stream[A] = Stream.cons(a, constant(a))

  def from(n: Int): Stream[Int] = Stream.cons(n, from(n+1))

  def fibs: Stream[Int] = {
    def fibsMN(m: Int, n: Int): Stream[Int] = Stream.cons(n, fibsMN(n, m+n))
    Stream.cons(0, fibsMN(0,1))
  }

  // ---

  def unfold[A,S](z:S)(f: S => Option[(A,S)]): Stream[A] = {
    def produce(s: S): Stream[A] = f(s) match {
      case None => Stream.empty
      case Some((a,next)) => Stream.cons(a, produce(next))
    }
    produce(z)
  }

  def fibsUnfold: Stream[Int] =
    unfold[Int, (Int, Int)]((0,1)){ case (m,n) => Some(m, (n, m+n)) }

  def fromUnfold(n: Int): Stream[Int] =
    unfold[Int, Int](n)(n => Some((n, n+1)))

  def constantUnfold[A](a: A): Stream[A] =
    unfold[A,A](a)(a => Some((a,a)))

  def onesUnfold: Stream[Int] =
    unfold[Int, Unit]()(_ => Some((1, ())))

  // ---
  def startsWith[A](s1: Stream[A], s2: Stream[A]): Boolean =
    unfold[Boolean, (Stream[A], Stream[A])]((s1, s2)) {
      case (Empty, Empty) => None
      case (Empty, _) => Some(false, (Stream.empty, Stream.empty))
      case (_, Empty) => None
      case (Cons(h1,t1), Cons(h2,t2)) => Some(h1() == h2(), (t1(),t2()))
    }.forAll(_ == true)
}
