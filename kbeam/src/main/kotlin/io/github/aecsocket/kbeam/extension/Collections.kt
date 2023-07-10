package io.github.aecsocket.kbeam.extension

/** Clears this collection and returns the previous values as a list. */
fun <E> MutableCollection<E>.swapList(): MutableList<E> {
  val res = toMutableList()
  clear()
  return res
}

/** Clears this collection and returns the previous values as a set. */
fun <E> MutableCollection<E>.swapSet(): MutableSet<E> {
  val res = toMutableSet()
  clear()
  return res
}
