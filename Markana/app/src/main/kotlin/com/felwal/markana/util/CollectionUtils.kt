package com.felwal.markana.util

fun <E> MutableCollection<E>.toggleInclusion(element: E) =
    if (contains(element)) remove(element) else add(element)

fun <E> MutableList<E>.replace(oldElement: E, newElement: E) =
    set(indexOf(oldElement), newElement)

fun <E> MutableCollection<E>.removeAll() = removeAll(this)

inline fun <reified E> MutableList<E>.toNullable(): MutableList<E?> = mutableListOf(*toTypedArray())

fun <E> MutableCollection<E?>.fillUp(value: E?, toSize: Int) =
    repeat(toSize - size) { add(value) }

fun <E> MutableCollection<E?>.crop(toSize: Int) =
    repeat(size - toSize) { remove(elementAt(size - 1)) }

fun <E> MutableCollection<E?>.cropUp(value: E?, toSize: Int) {
    if (size < toSize) fillUp(value, toSize)
    else if (size > toSize) crop(toSize)
}

fun BooleanArray?.orEmpty(): BooleanArray = this ?: BooleanArray(0)

val <A, B> Array<out Pair<A, B>>.firsts: List<A> get() = map { it.first }

val <A, B> Array<out Pair<A, B>>.seconds: List<B> get() = map { it.second }