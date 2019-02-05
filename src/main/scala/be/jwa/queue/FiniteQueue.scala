package be.jwa.queue

import scala.collection.mutable

class FiniteQueue[A](limit: Int) extends mutable.Queue[A] {

  override def enqueue(elems: A*): scala.Unit = {
    this ++= elems
    while (super.size > limit) {
      super.dequeue()
    }
  }

  override def +=(elem1: A, elem2: A, elems: A*): FiniteQueue.this.type = {
    enqueue(elem1)
    enqueue(elem2)
    this ++= elems
    while (super.size > limit) {
      super.dequeue()
    }
    this
  }
}