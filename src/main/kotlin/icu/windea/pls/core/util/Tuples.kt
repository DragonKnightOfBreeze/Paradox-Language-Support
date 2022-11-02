@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

typealias Tuple2<A, B> = Pair<A, B>

typealias TypedTuple2<T> = Pair<T, T>

typealias Tuple3<A, B, C> = Triple<A, B, C>

typealias TypedTuple3<T> = Triple<T, T, T>

fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)

fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)