package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class MapExtensionsPureTest {
    @Test
    fun orNull_on_map() {
        assertNull(emptyMap<String, Int>().orNull())
        val m = mapOf("a" to 1)
        assertSame(m, m.orNull())
    }

    @Test
    fun asMutable_and_optimized_and_synced() {
        val m = mutableMapOf("a" to 1)
        val mm = m.asMutable()
        mm["b"] = 2
        assertEquals(mapOf("a" to 1, "b" to 2), mm)

        assertTrue(emptyMap<String, Int>().optimized().isEmpty())
        assertEquals(mapOf("x" to 1), mapOf("x" to 1).optimized())

        val sync = mm.synced()
        sync["c"] = 3
        assertEquals(3, sync["c"])
    }

    @Test
    fun getOrInit_for_list_and_map() {
        val m1 = mutableMapOf<String, MutableList<Int>>()
        val l1 = m1.getOrInit("k")
        l1 += 1
        assertSame(l1, m1["k"])
        assertEquals(listOf(1), m1["k"])

        val m2 = mutableMapOf<String, MutableMap<String, Int>>()
        val level2 = m2.getOrInit("outer")
        level2["inner"] = 5
        assertEquals(5, m2["outer"]?.get("inner"))
    }

    @Test
    fun map_mapToArray_and_process() {
        val m = linkedMapOf("a" to 1, "b" to 2)
        val arr = m.mapToArray { (k, v) -> "$k=$v" }
        assertArrayEquals(arrayOf("a=1", "b=2"), arr)

        var seen = mutableListOf<String>()
        val all = m.process { e ->
            seen += "${e.key}:${e.value}"
            true
        }
        assertTrue(all)
        assertEquals(listOf("a:1", "b:2"), seen)

        seen = mutableListOf()
        val short = m.process { e ->
            seen += "${e.key}:${e.value}"
            false
        }
        assertFalse(short)
        assertEquals(listOf("a:1"), seen)
    }

    private class Holder {
        val backing = mutableMapOf<String, Int>()
        var count by (backing withDefault 1)
    }

    @Test
    fun withDefault_delegate_inserts_default_and_updates() {
        val h = Holder()
        // default inserted at delegate binding time
        assertEquals(1, h.backing["count"])
        // read via delegate
        assertEquals(1, h.count)
        // write via delegate
        h.count = 7
        assertEquals(7, h.backing["count"])
    }
}
