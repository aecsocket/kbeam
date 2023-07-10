package io.github.aecsocket.kbeam

import java.util.function.Consumer

/**
 * An interface for registering event listeners for the event type [E].
 *
 * This version of the interface is intended to be exposed to consumers which wish to register a
 * listener for an event. A private [OwnedEventDispatch] should be stored internally in order to
 * send an event to the listeners.
 *
 * The listeners are internally sorted by priority on insertion.
 *
 * To create an instance, use the top-level function returning an [OwnedEventDispatch].
 */
interface EventDispatch<E> {
  /**
   * Registers a listener to run when an event is called.
   *
   * @param priority When this listener will be run in relation to other listeners. A lower priority
   *   runs earlier; a higher priority runs later and has the "final say" on the event. The default
   *   priority is `0`.
   * @param fn The listener function to run.
   */
  operator fun invoke(priority: Int = 0, fn: Consumer<E>)
}

/**
 * An [EventDispatch] which allows consumers to call an event to all registered listeners.
 *
 * This version of the interface should be stored internally by a class, and only allow consumers
 * public access to the [EventDispatch] object, which does not allow dispatching events.
 */
interface OwnedEventDispatch<E> : EventDispatch<E> {
  /**
   * Calls the event [E], or any subtype [T], allowing all registered listeners to view and/or
   * modify it. Returns the [T] that was passed in.
   */
  fun <T : E> dispatch(event: T): T
}

/** Creates a new [OwnedEventDispatch] instance with no registered listeners. */
@Suppress("FunctionName") fun <E> EventDispatch(): OwnedEventDispatch<E> = EventDispatchImpl()

internal class EventDispatchImpl<E> internal constructor() : OwnedEventDispatch<E> {
  private class Listener<E>(
      val priority: Int,
      val fn: Consumer<E>,
  )

  private val listeners = ArrayList<Listener<E>>()

  override fun invoke(priority: Int, fn: Consumer<E>) {
    listeners += Listener(priority, fn)
    listeners.sortBy { it.priority }
  }

  override fun <T : E> dispatch(event: T): T {
    listeners.forEach { it.fn.accept(event) }
    return event
  }
}
