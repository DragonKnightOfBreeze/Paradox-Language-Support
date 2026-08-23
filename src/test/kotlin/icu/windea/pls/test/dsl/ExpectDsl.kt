@file:Suppress("unused")

package icu.windea.pls.test.dsl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * 这个 DSL 提供了一组作用域方法，从而支持以扩展函数的形式进行断言。
 *
 * 这些函数直接返回其接收者，并在必要时适用 kotlin Contract 以及更改返回类型。
 *
 * 示例：
 * - `expectScope { result.expectNotNull().someMethodForNonNullType() }`
 * - `expectScope { result.expectIs<SomeType>().someMethodForThisType() }`
 */
@DslMarker
annotation class ExpectDsl

/**
 * @see ExpectDsl
 */
inline fun <R> expectScope(block: ExpectScope.() -> R): R = ExpectScope.block()

/**
 * @see ExpectDsl
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
@ExpectDsl
object ExpectScope {
    inline fun Boolean.expectTrue(message: String? = null): Boolean {
        contract { returns() implies this@expectTrue }
        assertTrue(this, message)
        return true
    }

    inline fun Boolean.expectFalse(message: String? = null): Boolean {
        contract { returns() implies (!this@expectFalse) }
        assertFalse(this, message)
        return false
    }

    inline fun <T> T.expectEquals(other: Any?, message: String? = null): T {
        assertEquals(other, this, message)
        return this
    }

    inline fun <T> T.expectNotEquals(other: Any?, message: String? = null): T {
        assertNotEquals(other, this, message)
        return this
    }

    inline fun <T> T.expectSame(other: Any?, message: String? = null): T {
        assertSame(other, this, message)
        return this
    }

    inline fun <T> T.expectNotSame(other: Any?, message: String? = null): T {
        assertNotSame(other, this, message)
        return this
    }

    inline fun <reified T> Any?.expectIs(message: String? = null): T {
        contract { returns() implies (this@expectIs is T) }
        assertIs<T>(this, message)
        return this
    }

    inline fun <T : Any> T?.expectNotNull(message: String? = null): T {
        contract { returns() implies (this@expectNotNull != null) }
        assertNotNull(this, message)
        return this
    }

    inline fun <T : Any> T?.expectNull(message: String? = null): T? {
        contract { returns() implies (this@expectNull == null) }
        assertNull(this, message)
        return this
    }

    inline fun <T> T.expectIn(other: Iterable<T>, message: String? = null): T {
        assertContains(other, this, message)
        return this
    }

    inline fun <T> T.expectIn(other: Sequence<T>, message: String? = null): T {
        assertContains(other, this, message)
        return this
    }

    inline fun <T> T.expectIn(other: Array<T>, message: String? = null): T {
        assertContains(other, this, message)
        return this
    }

    inline fun Int.expectIn(other: IntRange, message: String? = null): Int {
        assertContains(other, this, message)
        return this
    }

    inline fun Long.expectIn(other: LongRange, message: String? = null): Long {
        assertContains(other, this, message)
        return this
    }

    inline fun <T : Comparable<T>> T.expectIn(other: ClosedRange<T>, message: String? = null): T {
        assertContains(other, this, message)
        return this
    }

    inline fun <T : Comparable<T>> T.expectIn(other: OpenEndRange<T>, message: String? = null): T {
        assertContains(other, this, message)
        return this
    }

    inline fun <T> Iterable<T>?.expectContentEquals(other: Iterable<T>?, message: String? = null): Iterable<T>? {
        assertContentEquals(other, this, message)
        return this
    }

    inline fun <T> Sequence<T>?.expectContentEquals(other: Sequence<T>?, message: String? = null): Sequence<T>? {
        assertContentEquals(other, this, message)
        return this
    }

    inline fun <T> Array<T>?.expectContentEquals(other: Array<T>?, message: String? = null): Array<T>? {
        assertContentEquals(other, this, message)
        return this
    }

    inline fun <T> List<T>.expectOrderedEquals(vararg other: T): List<T> {
        assertEquals(other.toList(), this.toList())
        return this
    }

    inline fun <T> Collection<T>.expectUnorderedEquals(vararg other: T): Collection<T> {
        assertEquals(other.toSet(), this.toSet())
        return this
    }
}
