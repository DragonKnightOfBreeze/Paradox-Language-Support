package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionFastBuildersTest {
    // region buildImmutableList

    @Test
    fun buildImmutableList_basic_test() {
        val result = buildImmutableList(3) { it * 2 }
        assertEquals(listOf(0, 2, 4), result)
    }

    @Test
    fun buildImmutableList_empty_test() {
        val result = buildImmutableList(0) { throw IllegalStateException() }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun buildImmutableList_single_test() {
        val result = buildImmutableList(1) { 42 }
        assertEquals(listOf(42), result)
    }

    @Test
    fun buildImmutableList_immutability_test() {
        val result = buildImmutableList(3) { it }
        assertThrows(UnsupportedOperationException::class.java) {
            (result as MutableList).add(999)
        }
    }

    @Test
    fun buildImmutableList_negative_size_test() {
        assertThrows(IllegalArgumentException::class.java) {
            buildImmutableList(-1) { it }
        }
    }

    // endregion

    // region buildImmutableSet

    @Test
    fun buildImmutableSet_basic_test() {
        val result = buildImmutableSet(3) { it * 2 }
        assertEquals(setOf(0, 2, 4), result)
    }

    @Test
    fun buildImmutableSet_empty_test() {
        val result = buildImmutableSet(0) { throw IllegalStateException() }
        assertEquals(emptySet<Int>(), result)
    }

    @Test
    fun buildImmutableSet_single_test() {
        val result = buildImmutableSet(1) { 42 }
        assertEquals(setOf(42), result)
    }

    @Test
    fun buildImmutableSet_immutability_test() {
        val result = buildImmutableSet(3) { it }
        assertThrows(UnsupportedOperationException::class.java) {
            (result as MutableSet).add(999)
        }
    }

    @Test
    fun buildImmutableSet_negative_size_test() {
        assertThrows(IllegalArgumentException::class.java) {
            buildImmutableSet(-1) { it }
        }
    }

    // endregion

    // region asImmutableList

    @Test
    fun asImmutableList_basic_test() {
        val array = arrayOf<Any?>(1, 2, 3)
        val result = array.asImmutableList<Int>()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun asImmutableList_empty_test() {
        val array = emptyArray<Any?>()
        val result = array.asImmutableList<Int>()
        assertTrue(result.isEmpty())
    }

    @Test
    fun asImmutableList_single_test() {
        val array = arrayOf<Any?>("hello")
        val result = array.asImmutableList<String>()
        assertEquals(listOf("hello"), result)
    }

    @Test
    fun asImmutableList_immutability_test() {
        val array = arrayOf<Any?>(1, 2, 3)
        val result = array.asImmutableList<Int>()
        assertThrows(UnsupportedOperationException::class.java) {
            (result as MutableList).add(999)
        }
    }

    @Test
    fun asImmutableList_do_mutate_with_original_array_test() {
        val array = arrayOf<Any?>(1, 2, 3)
        val result = array.asImmutableList<Int>()
        array[0] = 999
        // 应随原数组修改而变化
        assertEquals(999, result[0])
    }

    // endregion
}
