package rak.collections

//this is probably better handled by a spliterator
class PrevBufferedIterator[T]( iter : Iterator[T] ) extends Iterator[T] {
  var prev : Option[T] = None


  override def hasNext: Boolean = iter.hasNext

  override def next(): T = {
    prev = Some(iter.next())
    prev.get
  }

  def takeIncluding(test : (T => Boolean)) : Iterator[T] = {
    return new IteratorWithCall(takeWhile(test), getPrev)
  }

  def getPrev() : T = {
    if (prev.isDefined) {
      prev.get
    } else {
      throw new NoSuchElementException
    }
  }

  private class IteratorWithCall(iter : Iterator[T], call : (() => T)) extends Iterator[T] {
    private var called = false

    override def hasNext: Boolean = iter.hasNext || !called

    override def next(): T =
      if (iter.hasNext) {
        iter.next
      } else if (!called) {
        called = true
        call()
      } else {
        throw new NoSuchElementException
      }
  }
}


