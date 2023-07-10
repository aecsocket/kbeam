package io.github.aecsocket.kbeam

import kotlin.reflect.KProperty

/**
 * A data holder for a [T] which keeps track of if the value has been modified. The type of [T]
 * should probably be immutable, so that changes can be accurately tracked.
 *
 * Can be used as a delegated property.
 */
class Dirty<T>(
    private var value: T,
) {
  /** If this value has been modified since the last [clean] call. */
  var isDirty: Boolean = false
    private set

  /** Gets the contained value. This **will not** change the dirty status. */
  fun get(): T = value

  /** Sets the contained value. This **will** mark the object as dirty. */
  fun set(value: T) {
    this.value = value
    isDirty = true
  }

  /**
   * Resets the dirty value back to false.
   *
   * @return The previous [isDirty] state.
   */
  fun clean(): Boolean {
    val wasDirty = isDirty
    isDirty = false
    return wasDirty
  }

  operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
}
