package io.github.aecsocket.kbeam

import kotlin.math.max

/**
 * A key used in a [GenArena], which is a wrapper around a [Long]. The upper 32 bits represent the
 * index at which this key points to ([index]), and the lower 32 bits represent the generation of
 * the item this key was made for ([gen]).
 */
@JvmInline
value class ArenaKey(val self: Long) {
  /** Creates a key from an index and generation pair. */
  constructor(index: Int, gen: Int) : this((index.toLong() shl 32) or gen.toLong())

  /** The index at which this key points to in an arena's entry list. */
  val index: Int
    get() = (self shr 32).toInt()

  /** The generation of the item this key was made for. */
  val gen: Int
    get() = self.toInt()

  /** Creates a new instance with a specified [index]. */
  fun withIndex(index: Int) =
      ArenaKey((self and 0x00000000_ffffffffu.toLong()) or (index.toLong() shl 32))

  /** Creates a new instance with a specified [gen]. */
  fun withGen(gen: Int) = ArenaKey((self and 0xffffffff_00000000u.toLong()) or gen.toLong())

  override fun toString() = "$index/$gen"
}

/**
 * A generational arena implementation, where elements are indexed by an index + generation pair.
 *
 * This is effectively a port of the Rust
 * [`generational_arena` crate](https://docs.rs/generational-arena/latest/generational_arena/), with
 * some implementation details changed. See the documentation there for info on how, why, and when to use this
 * over a simple list.
 */
interface GenArena<out E> : Iterable<GenArena.Entry<out E>> {
  /**
   * An item stored in an arena, indexed by its key.
   */
  data class Entry<E>(
      val key: ArenaKey,
      val value: E,
  ) {
    override fun toString() = "($key, $value)"
  }

  /**
   * Returns the size of the collection.
   */
  val size: Int

  /**
   * Returns `true` if the collection is empty (contains no elements), `false` otherwise.
   */
  fun isEmpty(): Boolean

  /**
   * Checks if an element with the specified key is contained in this collection.
   */
  operator fun contains(key: ArenaKey): Boolean

  /**
   * Gets an element at the specified key if it exists and has the same generation, otherwise
   * returns null.
   */
  operator fun get(key: ArenaKey): E?
}

/**
 * Returns `true` if the collection is not empty.
 */
fun <E> GenArena<E>.isNotEmpty() = !isEmpty()

/**
 * A generational arena that supports adding and removing elements.
 *
 * See [GenArena] for more details.
 */
interface MutableGenArena<E> : GenArena<E> {
  /**
   * Adds a value to the arena, potentially taking the index of a previous element, but definitely
   * being uniquely identifiable due to having a new generation.
   *
   * @return the key used to identify the element added.
   */
  fun add(value: E): ArenaKey

  /**
   * Removes a value from the arena based on the element's key.
   *
   * @return the value removed, or `null` if a value for that key did not exist.
   */
  fun remove(key: ArenaKey): E?

  /**
   * Removes all values from the arena and resets the generation.
   */
  fun clear()
}

/** Creates a new [MutableGenArena] with a default capacity. */
@Suppress("FunctionName")
fun <E> GenArena(capacity: Int = 4): MutableGenArena<E> = GenArenaImpl(capacity)

internal class GenArenaImpl<E> internal constructor(capacity: Int) : MutableGenArena<E> {
  private sealed interface Item<T> {
    data class Free<T>(val nextFree: Int) : Item<T>

    data class Occupied<T>(
        val gen: Int,
        val value: T,
    ) : Item<T>
  }

  private val items = ArrayList<Item<E>>()
  private var freeListHead = -1
  private var gen = 0
  override var size = 0

  init {
    require(capacity >= 0) { "capacity >= 0" }
    reserve(capacity)
  }

  override fun isEmpty(): Boolean = size == 0

  override fun contains(key: ArenaKey): Boolean = get(key) != null

  override fun get(key: ArenaKey): E? {
    when (val item = items[key.index]) {
      is Item.Free -> return null
      is Item.Occupied -> {
        if (key.gen != item.gen) return null
        return item.value
      }
    }
  }

  override fun iterator(): Iterator<GenArena.Entry<out E>> {
    return object : Iterator<GenArena.Entry<out E>> {
      private val iter = items.iterator().withIndex()
      private var next: GenArena.Entry<out E>? = compute()

      fun compute(): GenArena.Entry<out E>? {
        while (true) {
          if (!iter.hasNext()) return null
          val (index, entry) = iter.next()
          when (entry) {
            is Item.Free -> continue
            is Item.Occupied -> {
              return GenArena.Entry(
                  key = ArenaKey(index, entry.gen),
                  value = entry.value,
              )
            }
          }
        }
      }

      override fun hasNext(): Boolean = next != null

      override fun next(): GenArena.Entry<out E> {
        val next = next ?: throw NoSuchElementException()
        this.next = compute()
        return next
      }
    }
  }

  override fun add(value: E): ArenaKey {
    return tryInsert(value) ?: insertSlowPath(value)
  }

  override fun remove(key: ArenaKey): E? {
    if (key.index > items.size) return null

    when (val item = items[key.index]) {
      is Item.Free -> return null
      is Item.Occupied -> {
        if (key.gen != item.gen) return null
        val value = item.value
        items[key.index] = Item.Free(nextFree = freeListHead)
        gen += 1
        freeListHead = key.index
        size -= 1
        return value
      }
    }
  }

  override fun clear() {
    val end = items.size
    (0 until end).forEach { i ->
      items[i] =
          Item.Free(
              nextFree = if (i == end - 1) -1 else i + 1,
          )
    }
    freeListHead = 0
    size = 0
  }

  fun reserve(upTo: Int) {
    val start = items.size
    val end = max(start, upTo)
    val oldHead = freeListHead
    items.ensureCapacity(upTo)
    (start until end).map { i ->
      items.add(
          Item.Free(
              nextFree = if (i == end - 1) oldHead else i + 1,
          ),
      )
    }
    freeListHead = start
  }

  fun tryInsert(value: E): ArenaKey? {
    return tryAllocNextIndex()?.let { index ->
      items[index.index] =
          Item.Occupied(
              gen = gen,
              value = value,
          )
      index
    }
  }

  private fun insertSlowPath(value: E): ArenaKey {
    reserve(size * 2)
    return tryInsert(value)
        ?: throw IllegalStateException("Add will always succeed after reserving additional space")
  }

  private fun tryAllocNextIndex(): ArenaKey? {
    val freeIndex = freeListHead
    if (freeIndex == -1) return null

    when (val item = items[freeIndex]) {
      is Item.Occupied -> throw IllegalStateException("Corrupt free list")
      is Item.Free -> {
        freeListHead = item.nextFree
        size += 1
        return ArenaKey(freeIndex, gen)
      }
    }
  }

  override fun toString() =
      items
          .mapNotNull { it as? Item.Occupied }
          .joinToString(prefix = "[", postfix = "]#$gen") { "(${it.value})#${it.gen}" }
}
