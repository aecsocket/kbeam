package io.github.aecsocket.kbeam

import kotlin.test.*

class DirtyTest {
  @Test
  fun marking() {
    val dirty = Dirty(0)
    assertFalse(dirty.isDirty)

    dirty.get()
    assertFalse(dirty.isDirty)

    dirty.set(1)
    assertTrue(dirty.isDirty)
    assertEquals(1, dirty.get())

    assertTrue(dirty.clean())
    assertFalse(dirty.isDirty)
  }

  @Test
  fun delegating() {
    val dirty = Dirty(0)
    var x by dirty
    assertFalse(dirty.isDirty)

    @Suppress("UNUSED_VARIABLE") val y = x
    assertFalse(dirty.isDirty)

    x = 2
    assertTrue(dirty.isDirty)
    assertEquals(2, dirty.get())
  }
}
