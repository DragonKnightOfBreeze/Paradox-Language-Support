package icu.windea.pls.core.collections

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
        val arr = arrayOf(1, 2, 3).mapToArray { it * 2 }
        assertArrayEquals(arrayOf(2, 4, 6), arr)
    }
}
