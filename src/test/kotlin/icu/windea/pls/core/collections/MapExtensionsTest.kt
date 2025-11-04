package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class MapExtensionsTest {
    @Test
    fun orNull_on_map() {
        assertNull(emptyMap<String, Int>().orNull())
        val m = mapOf("a" to 1)
        assertSame(m, m.orNull())
    }

    @Test
    fun asMutable_and_and_synced() {
        val m = mutableMapOf("a" to 1)
        val mm = m.asMutable()
        mm["b"] = 2
        assertEquals(mapOf("a" to 1, "b" to 2), mm)

        val sync = mm.synced()
        sync["c"] = 3
        assertEquals(3, sync["c"])
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
    fun optimized_map_behaviors() {
        // empty -> emptyMap()
        val e = emptyMap<String, Int>()
        val eo = e.optimized()
        assertEquals(emptyMap<String, Int>(), eo)

        // singleton -> toMap() (content equal)
        val s = mapOf("a" to 1)
        val so = s.optimized()
        assertEquals(mapOf("a" to 1), so)

        // size > 1 -> same instance
        val m = linkedMapOf("a" to 1, "b" to 2)
        val mo = m.optimized()
        assertSame(m, mo)
    }

    @Test
    fun optimizedIfEmpty_map() {
        val e = emptyMap<String, Int>()
        val eo = e.optimizedIfEmpty()
        assertEquals(emptyMap<String, Int>(), eo)

        val m = mutableMapOf("a" to 1)
        assertSame(m, m.optimizedIfEmpty())
    }
}
