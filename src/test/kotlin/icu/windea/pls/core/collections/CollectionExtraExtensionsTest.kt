package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionExtraExtensionsTest {
    @Test
    fun getOne_getAll_test() {
        val map = mapOf("a" to listOf(1, 2, 3), "b" to emptyList())
        assertEquals(3, map.getOne("a"))
        assertNull(map.getOne("b"))
        assertNull(map.getOne("c"))
        assertEquals(listOf(1, 2, 3), map.getAll("a"))
        assertEquals(emptyList<Int>(), map.getAll("b"))
        assertEquals(emptyList<Int>(), map.getAll("c"))
    }

    @Test
    fun withDefault_delegate_inserts_default_and_updates_test() {
        val h = Holder()
        // default inserted at delegate binding time
        assertEquals(1, h.backing["count"])
        // read via delegate
        assertEquals(1, h.count)
        // write via delegate
        h.count = 7
        assertEquals(7, h.backing["count"])
    }

    private class Holder {
        val backing = mutableMapOf<String, Int>()
        var count by (backing withDefault 1)
    }
}
