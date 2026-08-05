package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionFastExtensionsTest {
    // region forEachFast

    @Test
    fun forEachFast_basic_test() {
        val list = listOf(1, 2, 3)
        val result = mutableListOf<Int>()
        list.forEachFast { result.add(it) }
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun forEachFast_empty_test() {
        val result = mutableListOf<Int>()
        emptyList<Int>().forEachFast { result.add(it) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun forEachFast_single_test() {
        val result = mutableListOf<Int>()
        listOf(42).forEachFast { result.add(it) }
        assertEquals(listOf(42), result)
    }

    @Test
    fun forEachFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3), listOf(1, 2, 3, 4))
        for (list in lists) {
            val fastResult = mutableListOf<Int>()
            list.forEachFast { fastResult.add(it) }
            val stdResult = mutableListOf<Int>()
            list.forEach { stdResult.add(it) }
            assertEquals("list=$list", stdResult, fastResult)
        }
    }

    // endregion

    // region forEachIndexedFast

    @Test
    fun forEachIndexedFast_basic_test() {
        val list = listOf("a", "b", "c")
        val indices = mutableListOf<Int>()
        val values = mutableListOf<String>()
        list.forEachIndexedFast { i, v -> indices.add(i); values.add(v) }
        assertEquals(listOf(0, 1, 2), indices)
        assertEquals(listOf("a", "b", "c"), values)
    }

    @Test
    fun forEachIndexedFast_empty_test() {
        val indices = mutableListOf<Int>()
        emptyList<String>().forEachIndexedFast { i, _ -> indices.add(i) }
        assertTrue(indices.isEmpty())
    }

    @Test
    fun forEachIndexedFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3))
        for (list in lists) {
            val fastResult = mutableListOf<Pair<Int, Int>>()
            list.forEachIndexedFast { i, v -> fastResult.add(i to v) }
            val stdResult = mutableListOf<Pair<Int, Int>>()
            list.forEachIndexed { i, v -> stdResult.add(i to v) }
            assertEquals("list=$list", stdResult, fastResult)
        }
    }

    // endregion

    // region forEachReversedFast

    @Test
    fun forEachReversedFast_basic_test() {
        val list = listOf(1, 2, 3)
        val result = mutableListOf<Int>()
        list.forEachReversedFast { result.add(it) }
        assertEquals(listOf(3, 2, 1), result)
    }

    @Test
    fun forEachReversedFast_empty_test() {
        val result = mutableListOf<Int>()
        emptyList<Int>().forEachReversedFast { result.add(it) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun forEachReversedFast_single_test() {
        val result = mutableListOf<Int>()
        listOf(42).forEachReversedFast { result.add(it) }
        assertEquals(listOf(42), result)
    }

    // endregion

    // region forEachReversedIndexedFast

    @Test
    fun forEachReversedIndexedFast_basic_test() {
        val list = listOf("a", "b", "c")
        val indices = mutableListOf<Int>()
        val values = mutableListOf<String>()
        list.forEachReversedIndexedFast { i, v -> indices.add(i); values.add(v) }
        assertEquals(listOf(2, 1, 0), indices)
        assertEquals(listOf("c", "b", "a"), values)
    }

    @Test
    fun forEachReversedIndexedFast_empty_test() {
        val indices = mutableListOf<Int>()
        emptyList<String>().forEachReversedIndexedFast { i, _ -> indices.add(i) }
        assertTrue(indices.isEmpty())
    }

    // endregion

    // region mapFast

    @Test
    fun mapFast_basic_test() {
        val result = listOf(1, 2, 3).mapFast { it * 2 }
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun mapFast_empty_test() {
        val result = emptyList<Int>().mapFast { it * 2 }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun mapFast_single_test() {
        val result = listOf(5).mapFast { it * 3 }
        assertEquals(listOf(15), result)
    }

    @Test
    fun mapFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3), listOf(1, 2, 3, 4, 5))
        for (list in lists) {
            val expected = list.map { it * 10 }
            val actual = list.mapFast { it * 10 }
            assertEquals("list=$list", expected, actual)
        }
    }

    @Test
    fun mapFast_nullable_values_test() {
        val list = listOf("a", "b")
        val result = list.mapFast { if (it == "a") "A" else "B" }
        assertEquals(listOf("A", "B"), result)
    }

    // endregion

    // region mapNotNullFast

    @Test
    fun mapNotNullFast_basic_test() {
        val result = listOf(1, 2, 3, 4).mapNotNullFast { if (it % 2 == 0) it else null }
        assertEquals(listOf(2, 4), result)
    }

    @Test
    fun mapNotNullFast_empty_test() {
        val result = emptyList<Int>().mapNotNullFast { it }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun mapNotNullFast_all_null_test() {
        val result = listOf(1, 2, 3).mapNotNullFast { null as Int? }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun mapNotNullFast_all_non_null_test() {
        val result = listOf(1, 2, 3).mapNotNullFast { it * 2 }
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun mapNotNullFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3, 4))
        val transforms = listOf<(Int) -> Int?>(
            { it * 2 },
            { if (it % 2 == 0) it else null },
            { null },
        )
        for (list in lists) {
            for (t in transforms) {
                val expected = list.mapNotNull(t)
                val actual = list.mapNotNullFast(t)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region flatMapFast

    @Test
    fun flatMapFast_basic_test() {
        val result = listOf(1, 2, 3).flatMapFast { listOf(it, it * 10) }
        assertEquals(listOf(1, 10, 2, 20, 3, 30), result)
    }

    @Test
    fun flatMapFast_empty_test() {
        val result = emptyList<Int>().flatMapFast { listOf(it) }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun flatMapFast_all_empty_collections_test() {
        val result = listOf(1, 2, 3).flatMapFast { emptyList<Int>() }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun flatMapFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3))
        val transforms = listOf<(Int) -> Collection<Int>>(
            { listOf(it) },
            { listOf(it, it * 2) },
            { emptyList() },
        )
        for (list in lists) {
            for (t in transforms) {
                val expected = list.flatMap(t)
                val actual = list.flatMapFast(t)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region filterFast

    @Test
    fun filterFast_basic_test() {
        val result = listOf(1, 2, 3, 4, 5).filterFast { it % 2 == 0 }
        assertEquals(listOf(2, 4), result)
    }

    @Test
    fun filterFast_empty_test() {
        val result = emptyList<Int>().filterFast { true }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun filterFast_all_match_test() {
        val result = listOf(1, 2, 3).filterFast { true }
        val expected = listOf(1, 2, 3)
        assertEquals(expected, result)
    }

    @Test
    fun filterFast_none_match_test() {
        val result = listOf(1, 2, 3).filterFast { false }
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun filterFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3, 4, 5))
        val predicates = listOf<(Int) -> Boolean>(
            { true },
            { false },
            { it % 2 == 0 },
            { it <= 3 },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.filter(p)
                val actual = list.filterFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region filterNotNullFast

    @Test
    fun filterNotNullFast_basic_test() {
        val result = listOf(1, null, 2, null, 3).filterNotNullFast()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun filterNotNullFast_empty_test() {
        val result = emptyList<Int?>().filterNotNullFast()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun filterNotNullFast_all_null_test() {
        val result = listOf(null, null, null).filterNotNullFast()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun filterNotNullFast_all_non_null_test() {
        val result = listOf(1, 2, 3).filterNotNullFast()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun filterNotNullFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, null, 2, null, 3), listOf(null, null))
        for (list in lists) {
            val expected = list.filterNotNull()
            val actual = list.filterNotNullFast()
            assertEquals("list=$list", expected, actual)
        }
    }

    // endregion

    // region filterIsInstanceFast

    @Test
    fun filterIsInstanceFast_basic_test() {
        val list: List<Any> = listOf(1, "a", 2, "b", 3)
        val result = list.filterIsInstanceFast<String>()
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun filterIsInstanceFast_with_predicate_test() {
        val list: List<Any> = listOf(1, "a", 2, "abc", 3)
        val result = list.filterIsInstanceFast<String> { it.length > 1 }
        assertEquals(listOf("abc"), result)
    }

    @Test
    fun filterIsInstanceFast_empty_test() {
        val result = emptyList<Any>().filterIsInstanceFast<String>()
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun filterIsInstanceFast_none_match_test() {
        val result = listOf<Any>(1, 2, 3).filterIsInstanceFast<String>()
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun filterIsInstanceFast_interface_consistency_test() {
        val lists: List<List<Any>> = listOf(emptyList(), listOf(1), listOf(1, "a", 2, "b"))
        for (list in lists) {
            val expected = list.filterIsInstance<String>()
            val actual = list.filterIsInstanceFast<String>()
            assertEquals("list=$list", expected, actual)
        }
    }

    // endregion

    // region findFast

    @Test
    fun findFast_basic_test() {
        val result = listOf(1, 2, 3, 4).findFast { it > 2 }
        assertEquals(3, result)
    }

    @Test
    fun findFast_not_found_test() {
        val result = listOf(1, 2, 3).findFast { it > 10 }
        assertNull(result)
    }

    @Test
    fun findFast_empty_test() {
        val result = emptyList<Int>().findFast { true }
        assertNull(result)
    }

    @Test
    fun findFast_returns_first_match_test() {
        val result = listOf(1, 2, 3, 4).findFast { it % 2 == 0 }
        assertEquals(2, result)
    }

    @Test
    fun findFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3, 4, 5))
        val predicates = listOf<(Int) -> Boolean>(
            { it > 3 },
            { it % 2 == 0 },
            { it == 1 },
            { it == 99 },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.find(p)
                val actual = list.findFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region findLastFast

    @Test
    fun findLastFast_basic_test() {
        val result = listOf(1, 2, 3, 4).findLastFast { it < 4 }
        assertEquals(3, result)
    }

    @Test
    fun findLastFast_not_found_test() {
        val result = listOf(1, 2, 3).findLastFast { it > 10 }
        assertNull(result)
    }

    @Test
    fun findLastFast_empty_test() {
        val result = emptyList<Int>().findLastFast { true }
        assertNull(result)
    }

    @Test
    fun findLastFast_returns_last_match_test() {
        val result = listOf(1, 2, 3, 4).findLastFast { it % 2 == 0 }
        assertEquals(4, result)
    }

    @Test
    fun findLastFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3, 4, 5))
        val predicates = listOf<(Int) -> Boolean>(
            { it > 3 },
            { it % 2 == 0 },
            { it == 1 },
            { it == 99 },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.findLast(p)
                val actual = list.findLastFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region findIsInstanceFast

    @Test
    fun findIsInstanceFast_basic_test() {
        val list: List<Any> = listOf(1, "a", 2, "b")
        val result = list.findIsInstanceFast<String>()
        assertEquals("a", result)
    }

    @Test
    fun findIsInstanceFast_with_predicate_test() {
        val list: List<Any> = listOf(1, "a", 2, "abc")
        val result = list.findIsInstanceFast<String> { it.length > 1 }
        assertEquals("abc", result)
    }

    @Test
    fun findIsInstanceFast_not_found_test() {
        val list: List<Any> = listOf(1, 2, 3)
        val result = list.findIsInstanceFast<String>()
        assertNull(result)
    }

    @Test
    fun findIsInstanceFast_empty_test() {
        val result = emptyList<Any>().findIsInstanceFast<String>()
        assertNull(result)
    }

    @Test
    fun findIsInstanceFast_interface_consistency_test() {
        val lists: List<List<Any>> = listOf(emptyList(), listOf(1), listOf(1, "a", 2, "abc", "x"))
        for (list in lists) {
            val expected = list.findIsInstance<String>()
            val actual = list.findIsInstanceFast<String>()
            assertEquals("list=$list", expected, actual)
        }
    }

    // endregion

    // region findLastIsInstanceFast

    @Test
    fun findLastIsInstanceFast_basic_test() {
        val list: List<Any> = listOf(1, "a", 2, "b")
        val result = list.findLastIsInstanceFast<String>()
        assertEquals("b", result)
    }

    @Test
    fun findLastIsInstanceFast_with_predicate_test() {
        val list: List<Any> = listOf(1, "abc", 2, "xy")
        val result = list.findLastIsInstanceFast<String> { it.length > 1 }
        assertEquals("xy", result) // 逆序遍历，"xy" 先找到
    }

    @Test
    fun findLastIsInstanceFast_not_found_test() {
        val list: List<Any> = listOf(1, 2, 3)
        val result = list.findLastIsInstanceFast<String>()
        assertNull(result)
    }

    @Test
    fun findLastIsInstanceFast_empty_test() {
        val result = emptyList<Any>().findLastIsInstanceFast<String>()
        assertNull(result)
    }

    @Test
    fun findLastIsInstanceFast_interface_consistency_test() {
        val lists: List<List<Any>> = listOf(emptyList(), listOf(1), listOf(1, "a", 2, "abc", "x"))
        for (list in lists) {
            val expected = list.findLastIsInstance<String>()
            val actual = list.findLastIsInstanceFast<String>()
            assertEquals("list=$list", expected, actual)
        }
    }

    // endregion

    // region allFast

    @Test
    fun allFast_basic_test() {
        assertTrue(listOf(2, 4, 6).allFast { it % 2 == 0 })
        assertFalse(listOf(2, 3, 4).allFast { it % 2 == 0 })
    }

    @Test
    fun allFast_empty_test() {
        assertTrue(emptyList<Int>().allFast { false })
    }

    @Test
    fun allFast_single_true_test() {
        assertTrue(listOf(1).allFast { it == 1 })
    }

    @Test
    fun allFast_single_false_test() {
        assertFalse(listOf(1).allFast { it != 1 })
    }

    @Test
    fun allFast_short_circuit_test() {
        var count = 0
        val result = listOf(1, 2, 3).allFast { count++; it < 2 }
        assertFalse(result)
        assertEquals(2, count) // 在第二个元素处提前终止
    }

    @Test
    fun allFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(2, 4, 6), listOf(1, 2, 3))
        val predicates = listOf<(Int) -> Boolean>(
            { it % 2 == 0 },
            { it > 0 },
            { false },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.all(p)
                val actual = list.allFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region anyFast

    @Test
    fun anyFast_basic_test() {
        assertTrue(listOf(1, 3, 4).anyFast { it % 2 == 0 })
        assertFalse(listOf(1, 3, 5).anyFast { it % 2 == 0 })
    }

    @Test
    fun anyFast_empty_test() {
        assertFalse(emptyList<Int>().anyFast { true })
    }

    @Test
    fun anyFast_short_circuit_test() {
        var count = 0
        val result = listOf(1, 2, 3).anyFast { count++; it > 1 }
        assertTrue(result)
        assertEquals(2, count) // 在第二个元素处提前终止
    }

    @Test
    fun anyFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 3, 5), listOf(1, 2, 3))
        val predicates = listOf<(Int) -> Boolean>(
            { it % 2 == 0 },
            { it > 0 },
            { false },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.any(p)
                val actual = list.anyFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region noneFast

    @Test
    fun noneFast_basic_test() {
        assertTrue(listOf(1, 3, 5).noneFast { it % 2 == 0 })
        assertFalse(listOf(1, 2, 3).noneFast { it % 2 == 0 })
    }

    @Test
    fun noneFast_empty_test() {
        assertTrue(emptyList<Int>().noneFast { true })
    }

    @Test
    fun noneFast_short_circuit_test() {
        var count = 0
        val result = listOf(1, 2, 3).noneFast { count++; it > 1 }
        assertFalse(result)
        assertEquals(2, count) // 在第二个元素处提前终止
    }

    @Test
    fun noneFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 3, 5), listOf(1, 2, 3))
        val predicates = listOf<(Int) -> Boolean>(
            { it % 2 == 0 },
            { it > 0 },
            { false },
        )
        for (list in lists) {
            for (p in predicates) {
                val expected = list.none(p)
                val actual = list.noneFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region processFast

    @Test
    fun processFast_all_process_test() {
        var sum = 0
        val result = listOf(1, 2, 3).processFast { sum += it; true }
        assertTrue(result)
        assertEquals(6, sum)
    }

    @Test
    fun processFast_short_circuit_test() {
        var sum = 0
        val result = listOf(1, 2, 3).processFast { sum += it; false }
        assertFalse(result)
        assertEquals(1, sum)
    }

    @Test
    fun processFast_empty_test() {
        val result = emptyList<Int>().processFast { true }
        assertTrue(result)
    }

    @Test
    fun processFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3))
        val processors = listOf<(Int) -> Boolean>(
            { true },
            { it < 2 },
            { false },
        )
        for (list in lists) {
            for (p in processors) {
                val expected = list.process(p)
                val actual = list.processFast(p)
                assertEquals("list=$list", expected, actual)
            }
        }
    }

    // endregion

    // region dropFast

    @Test
    fun dropFast_basic_test() {
        assertEquals(listOf(3, 4, 5), listOf(1, 2, 3, 4, 5).dropFast(2))
    }

    @Test
    fun dropFast_zero_test() {
        val list = listOf(1, 2, 3)
        assertSame(list, list.dropFast(0))
    }

    @Test
    fun dropFast_all_test() {
        val result = listOf(1, 2, 3).dropFast(3)
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun dropFast_more_than_size_test() {
        val result = listOf(1, 2).dropFast(5)
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun dropFast_single_remaining_test() {
        val result = listOf(1, 2, 3).dropFast(2)
        assertEquals(listOf(3), result)
    }

    @Test
    fun dropFast_empty_test() {
        val result = emptyList<Int>().dropFast(0)
        assertEquals(emptyList<Int>(), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun dropFast_negative_test() {
        listOf(1, 2).dropFast(-1)
    }

    @Test
    fun dropFast_interface_consistency_test() {
        val lists = listOf(emptyList(), listOf(1), listOf(1, 2, 3), listOf(1, 2, 3, 4, 5))
        val dropCounts = listOf(0, 1, 2, 10)
        for (list in lists) {
            for (n in dropCounts) {
                val expected = list.drop(n)
                val actual = list.dropFast(n)
                assertEquals("list=$list, n=$n", expected, actual)
            }
        }
    }

    // endregion
}
