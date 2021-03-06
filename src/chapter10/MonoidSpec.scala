package chapter10

import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import Monoid._

class MonoidSpec extends Specification with ScalaCheck {

  "Monoid" should {
    "intAddition is a Monoid" in {
      implicit val arbitraryInt = Arbitrary.arbitrary[Int]

      prop { (a1: Int, a2: Int, a3: Int) =>
        intAddition.op(a1, intAddition.zero) must equalTo(a1)
        intAddition.op(intAddition.zero, a2) must equalTo(a2)
        intAddition.op(a1, intAddition.op(a2,a3)) must equalTo(intAddition.op(intAddition.op(a1,a2),a3))
      }
    }

    "booleanOr is a Monoid" in {
      implicit val arbitraryBoolean = Arbitrary.arbitrary[Boolean]

      prop { (a1: Boolean, a2: Boolean, a3: Boolean) =>
        booleanOr.op(a1, booleanOr.zero) must equalTo(a1)
        booleanOr.op(booleanOr.zero, a2) must equalTo(a2)
        booleanOr.op(a1, booleanOr.op(a2, a3)) must equalTo(booleanOr.op(booleanOr.op(a1,a2),a3))
      }
    }

    "optionMonoid is a Monoid" in {
      implicit val arbitraryInt = Arbitrary.arbitrary[Int]
      implicit val arbitraryOption: Gen[Option[Int]] = arbitraryInt.map(i => if (i%2 == 0) None else Some(i))

      prop { (o1: Option[Int], o2: Option[Int], o3: Option[Int]) =>
        optionMonoid.op(o1, optionMonoid.zero) must equalTo(o1)
        optionMonoid.op(optionMonoid.zero, o2) must equalTo(o2)
        optionMonoid.op(o1, optionMonoid.op(o2, o3)) must equalTo(optionMonoid.op(optionMonoid.op(o1,o2),o3))
      }
    }

    "wordsMonoid is a Monoid" in {
      wordsMonoid.op("Hic", wordsMonoid.op("est ", "chorda ")) must equalTo("Hic est chorda")
      wordsMonoid.op("Hic ", wordsMonoid.op(" est", "chorda")) must equalTo("Hic est chorda")
    }

    "concatenate over stringMonoid" in {
      val ls = List("a", "b", "c")
      concatenate(ls, stringMonoid) must equalTo("abc")
      concatenate(ls, wordsMonoid) must equalTo("a b c")
    }

    /**
    "wordCountMonoid is a Monoid" in {
      implicit var arbitraryWordCount = ???
      prop { (w1: WordCount, w2: WordCount, w3: WordCount) =>
        wordCountMonoid.op(w1, wordCountMonoid.zero) must equalTo(w1)
        wordCountMonoid.op(wordCountMonoid.zero, w2) must equalTo(w2)
        wordCountMonoid.op(w1, wordCountMonoid.op(w2, w3)) must equalTo(wordCountMonoid.op(wordCountMonoid.op(w1,w2),w3))
      }
    }
    */

    "productMonoid is a Monoid" in {
      implicit val arbitraryBoolean = Arbitrary.arbitrary[Boolean]

      val prodMonoid = productMonoid(booleanOr, booleanAnd)
      prop { (a1: Boolean, a2: Boolean, a3: Boolean, a4: Boolean, a5: Boolean, a6: Boolean) =>
        prodMonoid.op((a1,a2), prodMonoid.zero) must equalTo((a1,a2))
        prodMonoid.op(prodMonoid.zero, (a1,a2)) must equalTo((a1,a2))
        prodMonoid.op((a1,a2), prodMonoid.op((a3,a4),(a5,a6))) must equalTo(prodMonoid.op(prodMonoid.op((a1,a2),(a3,a4)),(a5,a6)))
      }
    }
  }
}
