package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class CollectionBuildersTest {
    @Test
    fun mutableSet_without_comparator_test() {
        val s = MutableSet<Int>()
        s.addAll(listOf(3, 1, 2, 2))
        // uniqueness
        assertEquals(setOf(1, 2, 3), s.toSet())
    }

    @Test
    fun mutableSet_with_comparator_tree_order_test() {
        val s = MutableSet(Comparator.naturalOrder<Int>())
        s.addAll(listOf(3, 1, 2))
        // Should be TreeSet when comparator is provided
        assertTrue(s is TreeSet<*>)
        assertEquals(listOf(1, 2, 3), s.toList())
    }

    @Test
    fun merge_and_mergeTo_skip_null_or_empty_test() {
        val a = listOf(1, 2)
        val b = emptyList<Int>()
        val c: List<Int>? = null
        val result = merge(a, b, c)
        assertEquals(listOf(1, 2), result)

        val dest = mutableListOf<Int>()
        val ref = mergeTo(dest, a, b, c)
        assertSame(dest, ref)
        assertEquals(listOf(1, 2), dest)
    }

    @Test
    fun mutableStringSet_caseInsensitive_test() {
        val s = MutableStringSet(caseInsensitive = true)
        s.add("Foo")
        assertTrue(s.contains("foo"))
        s.add("FOO") // 大小写不敏感去重
        assertEquals(1, s.size)
    }

    @Test
    fun mutableStringKeyMap_caseInsensitive_test() {
        val m = MutableStringKeyMap<Int>(caseInsensitive = true)
        m["Foo"] = 1
        assertEquals(1, m["foo"])
        m["FOO"] = 2 // 覆盖
        assertEquals(1, m.size)
        assertEquals(2, m["foo"])
    }

    @Test
    fun caseInsensitiveStringHashingStrategy_test() {
        val s = CaseInsensitiveStringHashingStrategy
        assertEquals(s.hashCode("Foo"), s.hashCode("foo"))
        assertTrue(s.equals("Foo", "foo"))
        assertFalse(s.equals("Foo", "bar"))
        assertEquals(0, s.hashCode(null))
        assertTrue(s.equals(null, null))
    }
}
