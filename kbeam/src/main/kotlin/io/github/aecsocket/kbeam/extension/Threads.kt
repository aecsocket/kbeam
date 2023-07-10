package io.github.aecsocket.kbeam.extension

/**
 * Suspends the current thread until all given threads are complete. This method is semantically
 * equivalent to joining all given threads one by one with `forEach { it.join() }`.
 */
fun Iterable<Thread>.joinAll() = forEach { it.join() }
