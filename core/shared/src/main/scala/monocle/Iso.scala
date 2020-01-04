package monocle

import monocle.function._

abstract class Iso[A, B] extends Lens[A, B] with Prism[Nothing, A, B] { self =>
  override def modify(f: B => B): A => A =
    from => reverseGet(get(from))

  def compose[C](other: Iso[B, C]): Iso[A, C] = new Iso[A, C] {
    def get(from: A): C      = other.get(self.get(from))
    def reverseGet(to: C): A = self.reverseGet(other.reverseGet(to))
  }
  override def composeLens[C](other: Lens[B, C]): Lens[A, C] =
    (this: Lens[A, B]).compose(other)

  override def composePrism[E, C](other: Prism[E, B, C]): Prism[E, A, C] =
    (this: Prism[Nothing, A, B]).compose(other)

  override def asTarget[C](implicit ev: B =:= C): Iso[A, C] =
    asInstanceOf[Iso[A, C]]

  ///////////////////////////////////
  // dot syntax for optics typeclass
  ///////////////////////////////////

  override def _1(implicit ev: Field1[B]): Lens[A, ev.B] = first(ev)
  override def _2(implicit ev: Field2[B]): Lens[A, ev.B] = second(ev)
  override def _3(implicit ev: Field3[B]): Lens[A, ev.B] = third(ev)
  override def _4(implicit ev: Field4[B]): Lens[A, ev.B] = fourth(ev)
  override def _5(implicit ev: Field5[B]): Lens[A, ev.B] = fifth(ev)
  override def _6(implicit ev: Field6[B]): Lens[A, ev.B] = sixth(ev)

  override def first(implicit ev: Field1[B]): Lens[A, ev.B]  = compose(ev.first)
  override def second(implicit ev: Field2[B]): Lens[A, ev.B] = compose(ev.second)
  override def third(implicit ev: Field3[B]): Lens[A, ev.B]  = compose(ev.third)
  override def fourth(implicit ev: Field4[B]): Lens[A, ev.B] = compose(ev.fourth)
  override def fifth(implicit ev: Field5[B]): Lens[A, ev.B]  = compose(ev.fifth)
  override def sixth(implicit ev: Field6[B]): Lens[A, ev.B]  = compose(ev.sixth)

  override def at[I, C](i: I)(implicit ev: At.Aux[B, I, C]): Lens[A, Option[C]] = compose(ev.at(i))
  override def cons(implicit ev: Cons[B]): Prism[Unit, A, (ev.B, B)]            = compose(ev.cons)
  override def reverse(implicit ev: Reverse[B]): Iso[A, ev.B]                   = compose(ev.reverse)

  ///////////////////////////////////
  // dot syntax for standard types
  ///////////////////////////////////

  override def left[E, C](implicit ev: B =:= Either[E, C]): Prism[Unit, A, E] =
    asTarget[Either[E, C]].composePrism(Prism.left)
  override def right[E, C](implicit ev: B =:= Either[E, C]): Prism[Unit, A, C] =
    asTarget[Either[E, C]].composePrism(Prism.right)
  override def some[C](implicit ev: B =:= Option[C]): Prism[Unit, A, C] =
    asTarget[Option[C]].composePrism(Prism.some)
}

object Iso {
  def apply[A, B](_get: A => B)(_reverseGet: B => A): Iso[A, B] =
    new Iso[A, B] {
      def get(from: A): B      = _get(from)
      def reverseGet(to: B): A = _reverseGet(to)
    }

  def reverse[A, B](implicit ev: Reverse.Aux[A, B]): Iso[A, B] =
    ev.reverse

  def id[A]: Iso[A, A] =
    Iso[A, A](identity)(identity)
}
