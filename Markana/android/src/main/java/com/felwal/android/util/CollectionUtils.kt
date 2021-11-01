package com.felwal.android.util

fun <E> MutableCollection<E>.toggleInclusion(element: E) =
    if (contains(element)) remove(element) else add(element)

fun <E> MutableList<E>.replace(oldElement: E, newElement: E) =
    set(indexOf(oldElement), newElement)

fun <E> MutableCollection<E>.removeAll() = removeAll(this)

fun <E> MutableList<E>.removeAll(range: IntRange) = range.reversed().forEach { removeAt(it) }

fun <E> MutableList<E>.removeAllFrom(index: Int) = removeAll(index until size)

fun <E> MutableList<E>.removeAllTo(index: Int) = removeAll(0..index)

inline fun <reified E> MutableList<E>.toNullable(): MutableList<E?> = mutableListOf(*toTypedArray())

fun <E> List<E>.indicesOf(sublist: List<E>): List<Int> = sublist.map { indexOf(it) }

fun <E> Array<E>.indicesOf(subarray: Array<E>): IntArray = subarray.map { indexOf(it) }.toIntArray()

fun List<Int>.asIndicesOfTrueBooleans(size: Int): List<Boolean> {
    val itemStates = MutableList(size) { false }
    forEach { itemStates[it] = true }
    return itemStates
}

fun IntArray.asIndicesOfTrueBooleans(size: Int): BooleanArray {
    val itemStates = BooleanArray(size) { false }
    forEach { itemStates[it] = true }
    return itemStates
}

fun <E> List<E>.filter(include: BooleanArray): List<E> = filterIndexed { index, _ -> include[index] }

fun <E> MutableCollection<E?>.fillUp(value: E?, toSize: Int) =
    repeat(toSize - size) { add(value) }

fun <E> MutableCollection<E?>.crop(toSize: Int) =
    repeat(size - toSize) { remove(elementAt(size - 1)) }

fun <E> MutableCollection<E?>.cropUp(value: E?, toSize: Int) {
    if (size < toSize) fillUp(value, toSize)
    else if (size > toSize) crop(toSize)
}

inline fun <T, R> Iterable<T>.common(value: (T) -> R): R? {
    if (this is Collection && isEmpty()) return null

    val firstValue = value(elementAt(0))
    for (element in this) if (value(element) != firstValue) return null
    return firstValue
}

fun BooleanArray?.orEmpty(): BooleanArray = this ?: BooleanArray(0)

fun IntArray?.orEmpty(): IntArray = this ?: IntArray(0)

val <A, B> Array<out Pair<A, B>>.firsts: List<A> get() = map { it.first }

val <A, B> Array<out Pair<A, B>>.seconds: List<B> get() = map { it.second }