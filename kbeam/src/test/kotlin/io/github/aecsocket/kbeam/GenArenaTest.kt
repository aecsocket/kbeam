package io.github.aecsocket.kbeam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenArenaTest {
  @Test
  fun arenaKey() {
    val key = ArenaKey(1, 2)
    assertEquals(1, key.index)
    assertEquals(2, key.gen)

    assertEquals(3, key.withIndex(3).index)
    assertEquals(3, key.withGen(3).gen)
  }

  @Test
  fun addRemove() {
    val arena = GenArena<Int>()
    assertEquals(0, arena.size)
    assertTrue(arena.isEmpty())
    assertFalse(arena.isNotEmpty())

    val key = arena.add(5)
    assertEquals(1, arena.size)
    assertEquals(5, arena[key])

    arena.remove(key)
    assertEquals(0, arena.size)
    assertEquals(null, arena[key])
  }

  @Test
  fun aba() {
    val arena = GenArena<Int>()
    val keyA = arena.add(5)
    assertEquals(5, arena[keyA])

    arena.remove(keyA)
    assertEquals(null, arena[keyA])

    val keyB = arena.add(10)
    assertEquals(10, arena[keyB])
    assertEquals(null, arena[keyA])
  }

  @Test
  fun capacity() {
    val arena = GenArena<Int>()
    repeat(1000) { arena.add(it) }
    assertEquals(1000, arena.size)

    arena.clear()
    assertEquals(0, arena.size)

    repeat(1000) { arena.add(it) }
    assertEquals(1000, arena.size)
  }

  @Test
  fun iterator() {
    val arena = GenArena<Int>()
    repeat(3) { arena.add(it) }
    assertEquals(3, arena.size)
    assertEquals(
        listOf(
            GenArena.Entry(ArenaKey(0, 0), 0),
            GenArena.Entry(ArenaKey(1, 0), 1),
            GenArena.Entry(ArenaKey(2, 0), 2),
        ),
        arena.toList())

    arena.remove(ArenaKey(1, 0))
    assertEquals(
        listOf(
            GenArena.Entry(ArenaKey(0, 0), 0),
            GenArena.Entry(ArenaKey(2, 0), 2),
        ),
        arena.toList())
  }
}
