package io.github.aecsocket.kbeam.sync

import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * A locked resource on which reads and writes must be done only under the protection of a lock.
 *
 * To access the underlying [T], using [withLock] is preferred; if that is not possible, use [lock]
 * or [tryLock], then manually [unlock] afterward. A typical usage pattern is:
 * ```
 * fun addOne(res: Sync<Int>): Int {
 *   return res.withLock { value ->
 *     value + 1
 *   }
 * }
 * ```
 *
 * or
 *
 * ```
 * fun addOne(res: Sync<Int>): Int {
 *   val value: Int = res.lock()
 *   try {
 *     return value + 1
 *   } finally {
 *     res.unlock()
 *   }
 * }
 * ```
 */
interface Sync<T : Any> {
  /**
   * **Unsafe:** gives access to the underlying [T] regardless of lock status.
   *
   * This must be used with care!
   */
  fun leak(): T

  /**
   * Locks the resource if it is unlocked, or blocks until a lock is acquired, returning the [T]
   * once obtained.
   *
   * This must be paired with an [unlock] call later.
   */
  fun lock(): T

  /**
   * Attempts to acquire the lock right now if it is unlocked. If successful, returns the [T];
   * otherwise if the lock was already obtained, returns null.
   *
   * This must be paired with an [unlock] call later.
   */
  fun tryLock(): T?

  /**
   * Attempts to acquire the lock for the next duration provided. If successful, returns the [T];
   * otherwise if the lock was already obtained, returns null.
   *
   * This must be paired with an [unlock] call later.
   */
  fun tryLock(time: Long, unit: TimeUnit): T?

  /**
   * Unlocks the lock of this resource.
   *
   * This must be paired with a [lock] or [tryLock] call before.
   */
  fun unlock()

  /**
   * Acquires the lock, blocking if it is already locked, and calls the function provided with the
   * [T]. This method handles locking and unlocking.
   */
  fun <R> withLock(block: (T) -> R): R

  /**
   * Acquires the lock, blocking if it is already locked, and calls the function provided with the
   * [T]. This method handles locking and unlocking.
   *
   * This method is provided as a convenience for Java code to not have to return a [Unit].
   */
  fun withLock(block: Consumer<T>) = withLock { block.accept(it) }
}
