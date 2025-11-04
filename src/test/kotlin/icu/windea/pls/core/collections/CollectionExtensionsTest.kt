package icu.windea.pls.core.collections

import org.junit.Assert
import org.junit.Test

class CollectionExtensionsTest {
    @Test
    fun orNull_on_collection() {
        Assert.assertNull(emptyList<String>().orNull())
        val list = listOf("a")
        Assert.assertSame(list, list.orNull())
    }

    @Test
    fun toListOrThis_and_toSetOrThis_identity_and_copy() {
        val list = listOf(1, 2)
        Assert.assertSame(list, list.toListOrThis())

        val set: Set<Int> = linkedSetOf(1, 2)
        val asList = set.toListOrThis()
        Assert.assertEquals(listOf(1, 2), asList)

        val asSet = set.toSetOrThis()
        Assert.assertSame(set, asSet)
    }

    @Test
    fun asMutable_safe_cast_on_mutable_collections() {
        val list = mutableListOf(1, 2)
        val m = list.asMutable()
        m.add(3)
        Assert.assertEquals(listOf(1, 2, 3), m)

        val set = mutableSetOf(1, 2)
        val ms = set.asMutable()
        ms.add(3)
        Assert.assertEquals(setOf(1, 2, 3), ms)
    }

    @Test
    fun filterIsInstance_and_findIsInstance() {
        val list: List<Any?> = listOf(1, "a", null, "abc", 2)
        val onlyOneChar = list.filterIsInstance<String> { it.length == 1 }
        Assert.assertEquals(listOf("a"), onlyOneChar)

        val firstLen3 = list.findIsInstance<String> { it.length == 3 }
        Assert.assertEquals("abc", firstLen3)

        val none = list.findIsInstance<String> { it.length == 4 }
        Assert.assertNull(none)
    }

    @Test
    fun mapToArray_for_list_and_collection() {
        val arr1 = listOf(1, 2, 3).mapToArray { it * 2 }
        Assert.assertArrayEquals(arrayOf(2, 4, 6), arr1)

        val s = linkedSetOf(1, 2, 3)
        val arr2 = (s as Collection<Int>).mapToArray { it + 1 }
        Assert.assertArrayEquals(arrayOf(2, 3, 4), arr2)
    }

    @Test
    fun process_short_circuit() {
        val list = listOf(1, 2, 3)
        Assert.assertFalse(list.process { it < 3 })
        Assert.assertTrue(list.process { it <= 3 })
    }

    @Test
    fun pinned_and_pinnedLast() {
        val list = listOf(1, 2, 3, 4)
        Assert.assertEquals(listOf(2, 4, 1, 3), list.pinned { it % 2 == 0 })
        Assert.assertEquals(listOf(1, 3, 2, 4), list.pinnedLast { it % 2 == 0 })
    }

    @Test
    fun chunkedBy_emptyString() {
        val list = listOf("a","b","","c","","","d")
        Assert.assertEquals(listOf(listOf("a","b"), listOf("c"), listOf(), listOf("d")), list.chunkedBy { it.isEmpty() })
    }

    // region helpers
    private fun <T> iterableOf(vararg elements: T): Iterable<T> = object : Iterable<T> {
        override fun iterator(): Iterator<T> = elements.asList().iterator()
    }
    // endregion

    @Test
    fun pinned_edgeCases() {
        // empty list
        Assert.assertEquals(emptyList<Int>(), emptyList<Int>().pinned { it % 2 == 0 })

        // single element list (Collection fast-path)
        Assert.assertEquals(listOf(1), listOf(1).pinned { it % 2 == 0 })
        Assert.assertEquals(listOf(1), listOf(1).pinned { true })

        // all match / none match
        Assert.assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinned { it > 0 })
        Assert.assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinned { it < 0 })

        // stable order for pinned and unpinned
        val mixed = listOf(3, 1, 4, 2, 5)
        Assert.assertEquals(listOf(4, 2, 3, 1, 5), mixed.pinned { it % 2 == 0 })

        // non-Collection Iterable
        Assert.assertEquals(emptyList<Int>(), iterableOf<Int>().pinned { true })
        Assert.assertEquals(listOf(1), iterableOf(1).pinned { it == 1 })
        Assert.assertEquals(listOf(1), iterableOf(1).pinned { false })
    }

    @Test
    fun pinnedLast_edgeCases() {
        // empty list
        Assert.assertEquals(emptyList<Int>(), emptyList<Int>().pinnedLast { it % 2 == 0 })

        // single element list (Collection fast-path)
        Assert.assertEquals(listOf(1), listOf(1).pinnedLast { it % 2 == 0 })
        Assert.assertEquals(listOf(1), listOf(1).pinnedLast { true })

        // all match / none match
        Assert.assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinnedLast { it > 0 })
        Assert.assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).pinnedLast { it < 0 })

        // stable order for pinned last
        val mixed = listOf(3, 1, 4, 2, 5)
        Assert.assertEquals(listOf(3, 1, 5, 4, 2), mixed.pinnedLast { it % 2 == 0 })

        // non-Collection Iterable
        Assert.assertEquals(emptyList<Int>(), iterableOf<Int>().pinnedLast { true })
        Assert.assertEquals(listOf(1), iterableOf(1).pinnedLast { it == 1 })
        Assert.assertEquals(listOf(1), iterableOf(1).pinnedLast { false })
    }

    @Test
    fun chunkedBy_empty_input_and_no_separators() {
        // empty input
        Assert.assertEquals(listOf<List<String>>(emptyList()), emptyList<String>().chunkedBy { it.isEmpty() })
        Assert.assertEquals(emptyList<List<String>>(), emptyList<String>().chunkedBy(keepEmpty = false) { it.isEmpty() })

        // no separators
        val noSep = listOf("a", "b")
        Assert.assertEquals(listOf(listOf("a", "b")), noSep.chunkedBy { it.isEmpty() })
        Assert.assertEquals(listOf(listOf("a", "b")), noSep.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_leading_trailing_and_only_separators() {
        // leading and trailing separators
        val leadTrail = listOf("", "a", "")
        Assert.assertEquals(listOf(listOf(), listOf("a"), listOf()), leadTrail.chunkedBy { it.isEmpty() })
        Assert.assertEquals(listOf(listOf("a")), leadTrail.chunkedBy(keepEmpty = false) { it.isEmpty() })

        // only separators => n separators produce n+1 empty chunks when keepEmpty = true
        val onlySep = listOf("", "", "")
        Assert.assertEquals(listOf<List<String>>(emptyList(), emptyList(), emptyList(), emptyList()), onlySep.chunkedBy { it.isEmpty() })
        Assert.assertEquals(emptyList<List<String>>(), onlySep.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_keepEmpty_false_on_mixed() {
        val list = listOf("a", "b", "", "c", "", "", "d")
        Assert.assertEquals(listOf(listOf("a", "b"), listOf("c"), listOf("d")), list.chunkedBy(keepEmpty = false) { it.isEmpty() })
    }

    @Test
    fun chunkedBy_numeric_separators() {
        val nums = listOf(0, 1, 0, 0, 2, 0)
        Assert.assertEquals(
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
            Assert.assertEquals(yes + no, c.pinned(p))
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
            Assert.assertEquals(no + yes, c.pinnedLast(p))
        }
    }

    @Test
    fun chunkedBy_flatten_invariant_keepEmpty_true_and_false() {
        val s = listOf("", "a", "", "b", "", "", "c", "")
        val predS = { x: String -> x.isEmpty() }
        val kTrue = s.chunkedBy(keepEmpty = true, predicate = predS)
        val kFalse = s.chunkedBy(keepEmpty = false, predicate = predS)
        Assert.assertEquals(s.filterNot(predS), kTrue.flatten())
        Assert.assertEquals(s.filterNot(predS), kFalse.flatten())

        val n = listOf(0, 1, 0, 2, 0, 3)
        val predN = { x: Int -> x == 0 }
        val kTrueN = n.chunkedBy(keepEmpty = true, predicate = predN)
        val kFalseN = n.chunkedBy(keepEmpty = false, predicate = predN)
        Assert.assertEquals(n.filterNot(predN), kTrueN.flatten())
        Assert.assertEquals(n.filterNot(predN), kFalseN.flatten())
    }

    @Test
    fun chunkedBy_chunk_count_matches_separators_plus_one_when_keepEmpty_true() {
        val s = listOf("", "a", "", "b", "", "", "c", "")
        val pred = { x: String -> x.isEmpty() }
        val sepCount = s.count(pred)
        val chunks = s.chunkedBy(keepEmpty = true, predicate = pred)
        Assert.assertEquals(sepCount + 1, chunks.size)
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
    fun optimized_list_behaviors() {
        // empty -> emptyList()
        val e = emptyList<Int>()
        val eo = e.optimized()
        Assert.assertEquals(emptyList<Int>(), eo)
        // singleton -> toList() (not necessarily same instance)
        val s = listOf(1)
        val so = s.optimized()
        Assert.assertEquals(listOf(1), so)
        // size > 1 -> same instance
        val m = mutableListOf(1, 2)
        val mo = m.optimized()
        Assert.assertSame(m, mo)
    }

    @Test
    fun optimized_set_behaviors() {
        // empty -> emptySet()
        val e = emptySet<Int>()
        val eo = e.optimized()
        Assert.assertEquals(emptySet<Int>(), eo)
        // singleton -> toSet() (not necessarily same instance)
        val s = setOf(1)
        val so = s.optimized()
        Assert.assertEquals(setOf(1), so)
        // size > 1 -> same instance
        val m: MutableSet<Int> = linkedSetOf(1, 2)
        val mo = m.optimized()
        Assert.assertSame(m, mo)
    }

    @Test
    fun optimizedIfEmpty_list_and_set() {
        // list
        val el = emptyList<Int>()
        val el2 = el.optimizedIfEmpty()
        Assert.assertEquals(emptyList<Int>(), el2)
        val l = mutableListOf(1)
        Assert.assertSame(l, l.optimizedIfEmpty())

        // set
        val es = emptySet<Int>()
        val es2 = es.optimizedIfEmpty()
        Assert.assertEquals(emptySet<Int>(), es2)
        val s = mutableSetOf(1)
        Assert.assertSame(s, s.optimizedIfEmpty())
    }

    @Test
    fun removePrefixOrNull_basic_and_edges() {
        val base = listOf(1, 2, 3)

        // empty prefix -> return this (identity)
        Assert.assertSame(base, base.removePrefixOrNull(emptyList()))

        // longer prefix -> null
        Assert.assertNull(base.removePrefixOrNull(listOf(1, 2, 3, 4)))

        // exact match -> empty list
        Assert.assertEquals(emptyList<Int>(), base.removePrefixOrNull(listOf(1, 2, 3)))

        // proper prefix -> tail
        Assert.assertEquals(listOf(3), base.removePrefixOrNull(listOf(1, 2)))

        // mismatch at first element
        Assert.assertNull(base.removePrefixOrNull(listOf(0)))

        // mismatch inside
        Assert.assertNull(base.removePrefixOrNull(listOf(1, 0)))
    }

    @Test
    fun removeSuffixOrNull_basic_and_edges() {
        val base = listOf(1, 2, 3)

        // empty suffix -> return this (identity)
        Assert.assertSame(base, base.removeSuffixOrNull(emptyList()))

        // longer suffix -> null
        Assert.assertNull(base.removeSuffixOrNull(listOf(0, 1, 2, 3)))

        // exact match -> empty list
        Assert.assertEquals(emptyList<Int>(), base.removeSuffixOrNull(listOf(1, 2, 3)))

        // proper suffix -> head
        Assert.assertEquals(listOf(1), base.removeSuffixOrNull(listOf(2, 3)))

        // mismatch at last element
        Assert.assertNull(base.removeSuffixOrNull(listOf(4)))

        // mismatch inside
        Assert.assertNull(base.removeSuffixOrNull(listOf(2, 4)))
    }
}
