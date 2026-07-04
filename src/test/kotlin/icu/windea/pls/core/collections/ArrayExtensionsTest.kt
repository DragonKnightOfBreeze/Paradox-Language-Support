package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class ArrayExtensionsTest {
    @Test
    fun orNull_on_array() {
        assertNull(emptyArray<String>().orNull())
        val arr = arrayOf("a")
        assertSame(arr, arr.orNull())
    }

    @Test
    fun mapToArray_on_array() {
        val empty = emptyArray<String>()

        assertTrue(arrayOf<String>().mapToArray(empty) { it.repeat(1) } === arrayOf<String>().mapToArray<String, String>(empty) { it.repeat(1) })

        val arr = arrayOf("a", "b", "c").mapToArray(empty) { it.repeat(1) }
        assertArrayEquals(arrayOf("a", "b", "c"), arr)
    }

    @Test
    fun mapToArray_on_list() {
        val empty = emptyArray<String>()

        assertTrue(listOf<String>().mapToArray(empty) { it.repeat(1) } === listOf<String>().mapToArray<String, String>(empty) { it.repeat(1) })

        val arr = listOf("a", "b", "c").mapToArray(empty) { it.repeat(1) }
        assertArrayEquals(arrayOf("a", "b", "c"), arr)
    }

    @Test
    fun mapToArray_on_set() {
        val empty = emptyArray<String>()

        assertTrue(setOf<String>().mapToArray(empty) { it.repeat(1) } === setOf<String>().mapToArray<String, String>(empty) { it.repeat(1) })

        val arr = setOf("a", "b", "c").mapToArray(empty) { it.repeat(1) }
        assertArrayEquals(arrayOf("a", "b", "c"), arr)
    }
}
