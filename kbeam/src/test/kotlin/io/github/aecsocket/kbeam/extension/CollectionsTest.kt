package io.github.aecsocket.kbeam.extension

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionsTest {
  @Test
  fun swapList() {
    val x = mutableListOf(1, 2, 3, 2, 1)
    assertEquals(listOf(1, 2, 3, 2, 1), x.swapList())
    assertTrue(x.isEmpty())
  }

  @Test
  fun swapSet() {
    val x = mutableSetOf(1, 2, 3)
    assertEquals(setOf(1, 2, 3), x.swapSet())
    assertTrue(x.isEmpty())
  }
}
