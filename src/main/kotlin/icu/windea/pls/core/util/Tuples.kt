@file:Suppress("unused")

package icu.windea.pls.core.util

/** 二元组类型别名，对应 Kotlin 的 [Pair]。*/
typealias Tuple2<A, B> = Pair<A, B>

/** 同类型的二元组。*/
typealias TypedTuple2<T> = Pair<T, T>

/** 三元组类型别名，对应 Kotlin 的 [Triple]。*/
typealias Tuple3<A, B, C> = Triple<A, B, C>

/** 同类型的三元组。*/
typealias TypedTuple3<T> = Triple<T, T, T>

/** 同类型的四元组。*/
typealias TypedTuple4<T> = Tuple4<T, T, T, T>

/**
 * 四元组。
 */
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

/** 将同类型的四元组转换为列表。*/
fun <T> Tuple4<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

/** 构造二元组。*/
fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)

/** 构造三元组。*/
fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)

/** 构造四元组。*/
fun <A, B, C, D> tupleOf(first: A, second: B, third: C, fourth: D) = Tuple4(first, second, third, fourth)
