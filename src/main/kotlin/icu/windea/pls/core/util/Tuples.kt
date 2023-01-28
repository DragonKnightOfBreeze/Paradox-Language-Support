@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

typealias Tuple2<A, B> = Pair<A, B>

typealias TypedTuple2<T> = Pair<T, T>

typealias Tuple3<A, B, C> = Triple<A, B, C>

typealias TypedTuple3<T> = Triple<T, T, T>

typealias TypedTuple4<T> = Tuple4<T, T, T, T>

data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun <T> Tuple4<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)

fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)

fun <A, B, C, D> tupleOf(first: A, second: B, third: C, fourth: D) = Tuple4(first, second, third, fourth)