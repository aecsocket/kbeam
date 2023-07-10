package io.github.aecsocket.kbeam

import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class TestEventDispatch {
  data class MyEvent(
      val value: Int,
  )

  @Test
  fun dispatch() {
    val onEvent = EventDispatch<MyEvent>()
    val value = AtomicInteger(-1)

    onEvent { event ->
      assertEquals(-1, value.get())
      value.set(event.value)
    }
    onEvent.dispatch(MyEvent(value = 3))
    assertEquals(3, value.get())
  }

  @Test
  fun priorityA() {
    val onEvent = EventDispatch<MyEvent>()
    val value = AtomicInteger(-1)

    onEvent(priority = -5) { event ->
      assertEquals(-1, value.get())
      value.set(event.value - 10)
    }
    onEvent(priority = 0) { event ->
      assertEquals(5, value.get())
      value.set(event.value)
    }
    onEvent(priority = 5) { event ->
      assertEquals(15, value.get())
      value.set(event.value + 10)
    }
    onEvent.dispatch(MyEvent(value = 15))
    assertEquals(25, value.get())
  }

  @Test
  fun priorityB() {
    val onEvent = EventDispatch<MyEvent>()
    val value = AtomicInteger(-1)

    // make sure registration order doesn't matter
    onEvent(priority = 5) { event ->
      assertEquals(15, value.get())
      value.set(event.value + 10)
    }
    onEvent(priority = -5) { event ->
      assertEquals(-1, value.get())
      value.set(event.value - 10)
    }
    onEvent(priority = 0) { event ->
      assertEquals(5, value.get())
      value.set(event.value)
    }
    onEvent.dispatch(MyEvent(value = 15))
    assertEquals(25, value.get())
  }
}
