package chapter10

trait Monoid[A] {
  def op(a1: A, a2: A): A
  def zero: A
}

object Monoid {
  val stringMonoid = new Monoid[String] {
    def op(a1: String, a2: String) = a1 + a2
    def zero = ""
  }

  def listMonoid[A] = new Monoid[List[A]] {
    def op(a1: List[A], a2: List[A]) = a1 ++ a2
    def zero = Nil
  }

  val intAddition = new Monoid[Int] {
    def op(a1: Int, a2: Int) = a1 + a2
    def zero = 0
  }

  val intMultiplication = new Monoid[Int] {
    def op(a1: Int, a2: Int) = a1 * a2
    def zero = 1
  }

  val booleanAnd = new Monoid[Boolean] {
    def op(a1: Boolean, a2: Boolean) = a1 && a2
    def zero = true
  }

  val booleanOr = new Monoid[Boolean] {
    def op(a1: Boolean, a2: Boolean) = a1 || a2
    def zero = false
  }

  def optionMonoid[A] = new Monoid[Option[A]] {
    def op(a1: Option[A], a2: Option[A]): Option[A] = (a1,a2) match {
      case (Some(_), _) => a1
      case (_, _) => a2
    }
    def zero = None
  }

  type EndoFun[A] = A => A
  def endoMonoid[A] = new Monoid[EndoFun[A]] {
    def op(f1: EndoFun[A], f2: EndoFun[A]): EndoFun[A] = a => (f1 andThen f2)(a)
    def zero: EndoFun[A] = a => a
  }

  val wordsMonoid: Monoid[String] = new Monoid[String] {
    def op(a1: String, a2: String): String = (a1.trim + " " + a2.trim).trim
    def zero: String = ""
  }

  def concatenate[A](as: List[A], m: Monoid[A]): A = as.foldLeft(m.zero)(m.op)

  def foldMap[A,B](as: List[A], m: Monoid[B])(f: A => B): B = concatenate(as.map(f), m)

  val wordCountMonoid = new Monoid[WordCount] {
    def op(w1: WordCount, w2: WordCount): WordCount = ???
    def zero: WordCount = ???
  }

  def productMonoid[A,B](ma: Monoid[A], mb: Monoid[B]): Monoid[(A,B)] = new Monoid[(A,B)] {
    override def op(a1: (A, B), a2: (A, B)): (A, B) = (ma.op(a1._1, a2._1), mb.op(a1._2, a2._2))
    override def zero: (A, B) = (ma.zero, mb.zero)
  }
}