package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionExtensionsTest {
    @Test
    fun orNull_on_collection() {
        assertNull(emptyList<String>().orNull())
        val list = listOf("a")
        assertSame(list, list.orNull())
    }
    @Test
    fun orNull_on_map() {
        assertNull(emptyMap<String, Int>().orNull())
        val m = mapOf("a" to 1)
        assertSame(m, m.orNull())
    }

    @Test
    fun toListOrThis_and_toSetOrThis_identity_and_copy() {
        val list = listOf(1, 2)
        assertSame(list, list.toListOrThis())

        val set: Set<Int> = linkedSetOf(1, 2)
        val asList = set.toListOrThis()
        assertEquals(listOf(1, 2), asList)

        val asSet = set.toSetOrThis()
        assertSame(set, asSet)
    }

    @Test
    fun asMutable_safe_cast_on_mutable_collections() {
        val list = mutableListOf(1, 2)
        val m = list.asMutable()
        m.add(3)
        assertEquals(listOf(1, 2, 3), m)

        val set = mutableSetOf(1, 2)
        val ms = set.asMutable()
        ms.add(3)
        assertEquals(setOf(1, 2, 3), ms)
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
    fun filterIsInstance_and_findIsInstance() {
        val list: List<Any?> = listOf(1, "a", null, "abc", 2)
        val onlyOneChar = list.filterIsInstance<String> { it.length == 1 }
        assertEquals(listOf("a"), onlyOneChar)

        val firstLen3 = list.findIsInstance<String> { it.length == 3 }
        assertEquals("abc", firstLen3)

        val none = list.findIsInstance<String> { it.length == 4 }
        assertNull(none)
    }

    @Test
    fun mapToArray_for_list_and_collection() {
        val arr1 = listOf(1, 2, 3).mapToArray { it * 2 }
        assertArrayEquals(arrayOf(2, 4, 6), arr1)

        val s = linkedSetOf(1, 2, 3)
        val arr2 = (s as Collection<Int>).mapToArray { it + 1 }
        assertArrayEquals(arrayOf(2, 3, 4), arr2)
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


    @Test
    fun process_short_circuit() {
        val list = listOf(1, 2, 3)
        assertFalse(list.process { it < 3 })
        assertTrue(list.process { it <= 3 })
    }

    @Test
    fun pinned_and_pinnedLast() {
        val list = listOf(1, 2, 3, 4)
        assertEquals(listOf(2, 4, 1, 3), list.pinned { it % 2 == 0 })
        assertEquals(listOf(1, 3, 2, 4), list.pinnedLast { it % 2 == 0 })
    }

    @Test
    fun chunkedBy_emptyString() {
        val list = listOf("a", "b", "", "c", "", "", "d")
        assertEquals(listOf(listOf("a", "b"), listOf("c"), listOf(), listOf("d")), list.chunkedBy { it.isEmpty() })
    }

    // region helpers
    private fun <T> iterableOf(vararg elements: T): Iterable<T> = object : Iterable<T> {
        override fun iterator(): Iterator<T> = elements.asList().iterator()
    }
    // endregion

    @Test
    fun pinned_edgeCases() {
        // empty list
        assertEquals(emptyList<Int>(), emptyList<Int>().pinned { it % 2 == 0 })

        // single element list (Collection fast-path)
        assertEquals(listOf(1), listOf(1).pinned { it % 2 == 0 })
        assertEquals(listOf(1), listOf(1).pinned { true })

        // all match / none match
        assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinned { it > 0 })
        assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinned { it < 0 })

        // stable order for pinned and unpinned
        val mixed = listOf(3, 1, 4, 2, 5)
        assertEquals(listOf(4, 2, 3, 1, 5), mixed.pinned { it % 2 == 0 })

        // non-Collection Iterable
        assertEquals(emptyList<Int>(), iterableOf<Int>().pinned { true })
        assertEquals(listOf(1), iterableOf(1).pinned { it == 1 })
        assertEquals(listOf(1), iterableOf(1).pinned { false })
    }

    @Test
    fun pinnedLast_edgeCases() {
        // empty list
        assertEquals(emptyList<Int>(), emptyList<Int>().pinnedLast { it % 2 == 0 })

        // single element list (Collection fast-path)
        assertEquals(listOf(1), listOf(1).pinnedLast { it % 2 == 0 })
        assertEquals(listOf(1), listOf(1).pinnedLast { true })

        // all match / none match
        assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinnedLast { it > 0 })
        assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinnedLast { it < 0 })

        // stable order for pinned last
        val mixed = listOf(3, 1, 4, 2, 5)
        assertEquals(listOf(3, 1, 5, 4, 2), mixed.pinnedLast { it % 2 == 0 })

        // non-Collection Iterable
        assertEquals(emptyList<Int>(), iterableOf<Int>().pinnedLast { true })
        assertEquals(listOf(1), iterableOf(1).pinnedLast { it == 1 })
        assertEquals(listOf(1), iterableOf(1).pinnedLast { false })
    }

    @Test
    fun chunkedBy_empty_input_and_no_separators() {
        // empty input
        assertEquals(listOf<List<String>>(emptyList()), emptyList<String>().chunkedBy { it.isEmpty() })
        assertEquals(emptyList<List<String>>(), emptyList<String>().chunkedBy(keepEmpty = false) { it.isEmpty() })

        // no separators
        val noSep = listOf("a", "b")
        assertEquals(listOf(listOf("a", "b")), noSep.chunkedBy { it.isEmpty() })
        assertEquals(listOf(listOf("a", "b")), noSep.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_leading_trailing_and_only_separators() {
        // leading and trailing separators
        val leadTrail = listOf("", "a", "")
        assertEquals(listOf(listOf(), listOf("a"), listOf()), leadTrail.chunkedBy { it.isEmpty() })
        assertEquals(listOf(listOf("a")), leadTrail.chunkedBy(keepEmpty = false) { it.isEmpty() })

        // only separators => n separators produce n+1 empty chunks when keepEmpty = true
        val onlySep = listOf("", "", "")
        assertEquals(listOf<List<String>>(emptyList(), emptyList(), emptyList(), emptyList()), onlySep.chunkedBy { it.isEmpty() })
        assertEquals(emptyList<List<String>>(), onlySep.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_keepEmpty_false_on_mixed() {
        val list = listOf("a", "b", "", "c", "", "", "d")
        assertEquals(listOf(listOf("a", "b"), listOf("c"), listOf("d")), list.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_numeric_separators() {
        val nums = listOf(0, 1, 0, 0, 2, 0)
        assertEquals(
            listOf(emptyList(), listOf(1), emptyList(), listOf(2), emptyList()),
            nums.chunkedBy { it == 0 }
        )
    }

    @Test
    fun pinned_partition_equivalence() {
        val cases = listOf(
            listOf(1, 2, 3, 4),
            listOf(2, 2, 1, 1, 2),
            emptyList(),
            listOf(1)
        )
        val p = { x: Int -> x % 2 == 0 }
        for (c in cases) {
            val (yes, no) = c.partition(p)
            assertEquals(yes + no, c.pinned(p))
        }
    }

    @Test
    fun pinnedLast_partition_equivalence() {
        val cases = listOf(
            listOf(1, 2, 3, 4),
            listOf(2, 2, 1, 1, 2),
            emptyList(),
            listOf(1)
        )
        val p = { x: Int -> x > 2 }
        for (c in cases) {
            val (yes, no) = c.partition(p)
            assertEquals(no + yes, c.pinnedLast(p))
        }
    }

    @Test
    fun chunkedBy_flatten_invariant_keepEmpty_true_and_false() {
        val s = listOf("", "a", "", "b", "", "", "c", "")
        val predS = { x: String -> x.isEmpty() }
        val kTrue = s.chunkedBy(keepEmpty = true, predicate = predS)
        val kFalse = s.chunkedBy(keepEmpty = false, predicate = predS)
        assertEquals(s.filterNot(predS), kTrue.flatten())
        assertEquals(s.filterNot(predS), kFalse.flatten())

        val n = listOf(0, 1, 0, 2, 0, 3)
        val predN = { x: Int -> x == 0 }
        val kTrueN = n.chunkedBy(keepEmpty = true, predicate = predN)
        val kFalseN = n.chunkedBy(keepEmpty = false, predicate = predN)
        assertEquals(n.filterNot(predN), kTrueN.flatten())
        assertEquals(n.filterNot(predN), kFalseN.flatten())
    }

    @Test
    fun chunkedBy_chunk_count_matches_separators_plus_one_when_keepEmpty_true() {
        val s = listOf("", "a", "", "b", "", "", "c", "")
        val pred = { x: String -> x.isEmpty() }
        val sepCount = s.count(pred)
        val chunks = s.chunkedBy(keepEmpty = true, predicate = pred)
        assertEquals(sepCount + 1, chunks.size)
    }

    @Test(expected = IllegalStateException::class)
    fun pinned_predicate_exception_propagates() {
        listOf(1, 2, 3).pinned { if (it == 2) throw IllegalStateException("boom") else false }
    }

    @Test(expected = IllegalStateException::class)
    fun pinnedLast_predicate_exception_propagates() {
        listOf(1, 2, 3).pinnedLast { if (it == 2) throw IllegalStateException("boom") else false }
    }

    @Test(expected = IllegalStateException::class)
    fun chunkedBy_predicate_exception_propagates() {
        listOf(1, 2, 3).chunkedBy { if (it == 2) throw IllegalStateException("boom") else false }
    }

    @Test
    fun removePrefixOrNull_basic_and_edges() {
        val base = listOf(1, 2, 3)

        // empty prefix -> return this (identity)
        assertSame(base, base.removePrefixOrNull(emptyList()))

        // longer prefix -> null
        assertNull(base.removePrefixOrNull(listOf(1, 2, 3, 4)))

        // exact match -> empty list
        assertEquals(emptyList<Int>(), base.removePrefixOrNull(listOf(1, 2, 3)))

        // proper prefix -> tail
        assertEquals(listOf(3), base.removePrefixOrNull(listOf(1, 2)))

        // mismatch at first element
        assertNull(base.removePrefixOrNull(listOf(0)))

        // mismatch inside
        assertNull(base.removePrefixOrNull(listOf(1, 0)))

        // with wildcard
        assertEquals(listOf("c"), listOf("a", "b", "c").removePrefixOrNull(listOf("a", "*"), wildcard = "*"))
    }

    @Test
    fun removeSuffixOrNull_basic_and_edges() {
        val base = listOf(1, 2, 3)

        // empty suffix -> return this (identity)
        assertSame(base, base.removeSuffixOrNull(emptyList()))

        // longer suffix -> null
        assertNull(base.removeSuffixOrNull(listOf(0, 1, 2, 3)))

        // exact match -> empty list
        assertEquals(emptyList<Int>(), base.removeSuffixOrNull(listOf(1, 2, 3)))

        // proper suffix -> head
        assertEquals(listOf(1), base.removeSuffixOrNull(listOf(2, 3)))

        // mismatch at last element
        assertNull(base.removeSuffixOrNull(listOf(4)))

        // mismatch inside
        assertNull(base.removeSuffixOrNull(listOf(2, 4)))

        // with wildcard
        assertEquals(listOf("a"), listOf("a", "b", "c").removeSuffixOrNull(listOf("b", "*"), wildcard = "*"))
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
