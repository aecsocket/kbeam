package io.github.aecsocket.kbeam.sync

import io.github.aecsocket.kbeam.extension.joinAll
import kotlin.RuntimeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LockedTest {
  data class Data(
      var x: Int,
  )

  @Test
  fun contestedAccess() {
    val locked = Locked(Data(0))

    (0 until 10)
        .map {
          Thread {
                repeat(1000) {
                  // failing code
                  // locked.leak().x += 1

                  // successful code
                  locked.withLock { data -> data.x += 1 }
                }
              }
              .apply { start() }
        }
        .joinAll()

    val data = assertNotNull(locked.tryLock())
    assertEquals(10_000, data.x)
  }

  @Test
  fun exceptionUnsafe() {
    val locked = Locked(Data(0))

    Thread {
          try {
            locked.lock()
            throw RuntimeException()
          } catch (_: RuntimeException) {}
        }
        .apply { start() }
        .join()

    assertNull(locked.tryLock())
  }

  @Test
  fun exceptionSafe() {
    val locked = Locked(Data(0))

    Thread {
          try {
            locked.withLock<Nothing> { throw RuntimeException() }
          } catch (_: RuntimeException) {}
        }
        .apply { start() }
        .join()

    assertNotNull(locked.tryLock())
  }
}
