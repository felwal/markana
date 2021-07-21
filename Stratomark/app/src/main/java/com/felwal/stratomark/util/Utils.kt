package com.felwal.stratomark.util

// Collection

fun <E> MutableCollection<E>.toggleInclusion(element: E) =
    if (contains(element)) remove(element) else add(element)

fun <E> MutableList<E>.replace(oldElement: E, newElement: E) =
    set(indexOf(oldElement), newElement)

fun <E> MutableCollection<E>.empty() = removeAll(this)

// any

fun <T, O> T.unless(equals: T, block: (it: T) -> O): O? = if (this != equals) block(this) else null

/**
 * Always returns [that]. The opposite of [over].
 */
infix fun <T> Any?.then(that: T): T = that

/**
 * Always returns [this]. The opposite of [then].
 */
infix fun <T> T.over(that: Any?): T = this

/**
 * Returns [this] if [that] isn't of that type.
 *
 * Useful in `when`s when you don't want to return at every branch:
 * `true defaults when (...)`
 */
inline infix fun <reified T> T.defaults(that: Any?): T = if (that is T) that else this

/**
 * Returns [that] if [this] isn't of that type.
 *
 * Useful in `when`s when you don't want to return at every branch:
 * `when (...) {...} default true`
 */
inline infix fun <reified T> Any?.default(that: T): T = if (this is T) this else that