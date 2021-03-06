package chapter6

trait RNG {
  def nextInt: (Int, RNG)
}

object RNG {

  type State[S,+A] = S => (A,S)
  type Rand[+A] = State[RNG,A]

  // State combinators

  def unit[S, A](a: A): State[S,A] = rng => (a, rng)

  def map[S,A,B](s: State[S,A])(f: A => B): State[S,B] = rng => {
    val (a, rng2) = s(rng)
    (f(a), rng2)
  }

  def flatMap[S,A,B](f: State[S,A])(g: A => State[S,B]): State[S,B] = rng => {
    val (a, rng2) = f(rng)
    g(a)(rng2)
  }

  def map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = rng => {
    val (a, rng2) = ra(rng)
    val (b, rng3) = rb(rng2)
    (f(a,b), rng3)
  }

  def mapViaFlatMap[A, B](s: Rand[A])(f: A => B): Rand[B] =
    flatMap(s)(a => unit(f(a)))

  def map2ViaFlatMap[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
    flatMap(ra)(a => flatMap(rb)(b => unit(f(a,b))))

  def sequence[A](fs: List[Rand[A]]): Rand[List[A]] = fs match {
    case Nil => throw new IllegalArgumentException
    case f :: Nil => { rng => {
        val (nextA, nextRng) = f(rng)
        (List(nextA), nextRng)
      }
    }
    case f :: fss => { rng => {
        val (nextA, nextRng) = f(rng)
        val (tail, lastRng) = sequence(fss)(nextRng)
        (nextA :: tail, lastRng)
      }
    }
  }

  // ---

  def positiveMax(n: Int): Rand[Int] = map(positiveInt)(i => i % n)

  def intDoubleViaMap2: Rand[(Int, Double)] =
    map2(positiveInt, double){ case (i,d) => (i,d) }

  def simple(seed: Long): RNG = new RNG {
    def nextInt = {
      val seed2 = (seed*0x5DEECE66DL + 0xBL) &
        ((1L << 48) - 1)
      ((seed2 >>> 16).asInstanceOf[Int], simple(seed2))
    }
  }

  def positiveInt(rng: RNG): (Int, RNG) = {
    val (i, nextRng) = rng.nextInt
    val positiveInt = if (i == Int.MinValue) (i+1).abs else i.abs
    (positiveInt, nextRng)
  }

  // generate a double between 0 and 1
  def double(rng: RNG): (Double, RNG) = {
    val (i, r) = positiveInt(rng)
    (i / (Int.MaxValue.toDouble + 1), r)
  }

  def doubleViaMap: Rand[Double] =
    map(positiveInt)(_ / (Int.MaxValue.toDouble + 1))

  def intDouble(rng: RNG): ((Int,Double), RNG) = {
    val (i, rng2) = rng.nextInt
    val (d, rng3) = double(rng2)
    ((i,d), rng3)
  }

  def doubleInt(rng: RNG): ((Double,Int), RNG) = {
    val (d, rng2) = double(rng)
    val (i, rng3) = rng2.nextInt
    ((d,i), rng3)
  }

  def double3(rng: RNG): ((Double,Double,Double), RNG) = {
    val (d1, rng1) = double(rng)
    val (d2, rng2) = double(rng1)
    val (d3, rng3) = double(rng2)

    ((d1, d2, d3), rng3)
  }

  def ints(count: Int)(rng: RNG): (List[Int], RNG) =
    if (count == 0) {
      (Nil, rng)
    } else {
      val (i, rng2) = rng.nextInt
      val (tail, nextRng) = ints(count-1)(rng2)
      (i :: tail, nextRng)
    }

  def intsViaSequence(count: Int): Rand[List[Int]] =
    sequence(List.fill[Rand[Int]](count)(positiveInt))
}