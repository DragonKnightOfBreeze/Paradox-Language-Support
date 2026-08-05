package icu.windea.pls.core

import org.junit.Assert.*
import org.junit.Test

class StdlibFastExtraExtensionsTest {
    // region matchesPath

    @Test
    fun matchesPath_basic_and_strict_test() {
        // 完全相同
        assertTrue("/a/b".matchesPath("/a/b", acceptSelf = true))
        assertFalse("/a/b".matchesPath("/a/b", acceptSelf = false))

        // 父子路径
        assertTrue("/a".matchesPath("/a/b"))
        assertTrue("/a/b".matchesPath("/a/b/c"))
        assertFalse("/a/b/c".matchesPath("/a/b"))

        // strict：仅直接父路径
        assertTrue("/a".matchesPath("/a/b", strict = true))
        assertFalse("/a".matchesPath("/a/b/c", strict = true))
    }

    @Test
    fun matchesPath_trim_test() {
        // 当 trim=true 时，仅去除接收者的首尾路径分隔符
        assertTrue("a/b/".matchesPath("a/b/c", trim = true))
        assertTrue("a/b".matchesPath("a/b/c", trim = false))
        assertFalse("a/b/".matchesPath("a-b/c", trim = true))

        assertFalse("/a/b/".matchesPath("/a/b/c", trim = true))
        assertFalse("/a/b/".matchesPath("/a/b/c", trim = false))
    }

    @Test
    fun matchesPath_edge_test() {
        // 非严格模式下，任意深度子路径均可
        assertTrue("/a".matchesPath("/a/b/c/d"))
        assertTrue("/a/b".matchesPath("/a/b/c"))

        // 前缀匹配但下一个字符不是路径分隔符
        assertFalse("/a/b".matchesPath("/a/bc"))
        assertFalse("/a/b".matchesPath("/ab"))

        // 前导路径分隔符不会被忽略
        assertFalse("/a/b".matchesPath("a/b/c"))
        assertFalse("/a".matchesPath("a/b"))

        // 接收者为空的退化情况：仅匹配以 "/" 起始的路径（且不匹配自身以外的空路径边界）
        assertTrue("".matchesPath(""))
        assertTrue("".matchesPath("/"))
        assertTrue("".matchesPath("/a/b"))
        assertFalse("".matchesPath("a/b"))
    }

    @Test
    fun matchesPath_short_paths_test() {
        // 空路径匹配自身
        assertTrue("".matchesPath("", acceptSelf = true))
        assertFalse("".matchesPath("", acceptSelf = false))

        // 单层路径匹配自身及子路径
        assertTrue("/a".matchesPath("/a"))
        assertTrue("/a".matchesPath("/a/b"))
        assertFalse("/a".matchesPath("/a", acceptSelf = false))
    }

    @Test
    fun matchesPath_boundary_test() {
        // 确认路径差异仅在分隔符后
        assertTrue("/game".matchesPath("/game/common"))
        assertFalse("/game".matchesPath("/gameplay"))
        assertFalse("/a/bb".matchesPath("/a/b/b"))
        // 尾随分隔符的路径
        assertFalse("/a/".matchesPath("/a/b"))
    }

    // endregion

    // region toDelimitedList

    @Test
    fun toDelimitedList_basic_test() {
        assertEquals(listOf("a", "b", "c"), "a,b,c".toDelimitedList())
        assertEquals(listOf("a"), "a".toDelimitedList())
        assertEquals(listOf("a", "b"), "a;b".toDelimitedList(';'))
        assertEquals(listOf("a", "b", "c"), "a;b;c".toDelimitedList(';'))
    }

    @Test
    fun toDelimitedList_trim_and_empty_test() {
        assertEquals(listOf("a", "b"), " a , b ".toDelimitedList())
        assertEquals(emptyList<String>(), "".toDelimitedList())
        assertEquals(emptyList<String>(), "   ".toDelimitedList())
        assertEquals(emptyList<String>(), " , , ".toDelimitedList())
        assertEquals(listOf("a", "b"), "a,,b".toDelimitedList())
        assertEquals(listOf("a"), ",a,".toDelimitedList())
        assertEquals(listOf("a", "b"), "a; b".toDelimitedList(';'))
    }

    @Test
    fun toDelimitedList_single_part_test() {
        // 仅一个有效部分（无分隔符或单个分隔符前后为空）
        assertEquals(listOf("abc"), "abc".toDelimitedList())
        assertEquals(listOf("abc"), " , abc".toDelimitedList())
        assertEquals(listOf("abc"), "abc , ".toDelimitedList())
    }

    @Test
    fun toDelimitedList_alternative_delimiters_test() {
        assertEquals(listOf("a", "b", "c"), "a.b.c".toDelimitedList('.'))
        assertEquals(listOf("x", "y"), "x y".toDelimitedList(' '))
        assertEquals(listOf("1", "2", "3"), "1-2-3".toDelimitedList('-'))
    }

    // endregion

    // region toDelimitedSet

    @Test
    fun toDelimitedSet_basic_test() {
        assertEquals(setOf("a", "b", "c"), "a,b,c".toDelimitedSet())
        assertEquals(setOf("a"), "a".toDelimitedSet())
        assertEquals(setOf("a", "b"), "a;b".toDelimitedSet(';'))
    }

    @Test
    fun toDelimitedSet_dedup_and_empty_test() {
        assertEquals(setOf("a"), "a,a".toDelimitedSet())
        assertEquals(setOf("a", "b"), "a,b,a".toDelimitedSet())
        assertEquals(emptySet<String>(), "".toDelimitedSet())
        assertEquals(emptySet<String>(), " , , ".toDelimitedSet())
        assertEquals(setOf("a", "b"), " a , b ".toDelimitedSet())
        assertEquals(setOf("a", "b"), "a;b".toDelimitedSet(';'))
    }

    @Test
    fun toDelimitedSet_ordering_preservation_test() {
        // Set 不保证顺序，但应包含所有唯一元素
        val result = "c,a,b,a".toDelimitedSet()
        assertEquals(3, result.size)
        assertTrue(result.containsAll(listOf("a", "b", "c")))
    }

    // endregion

    // region toDelimitedMutableList

    @Test
    fun toDelimitedMutableList_default_result_test() {
        assertEquals(mutableListOf("a", "b"), "a,b".toDelimitedMutableList())
        assertEquals(mutableListOf("a"), "a".toDelimitedMutableList())
        assertEquals(mutableListOf<String>(), "".toDelimitedMutableList())
        assertEquals(mutableListOf("a", "b"), " a , b ".toDelimitedMutableList())
    }

    @Test
    fun toDelimitedMutableList_with_result_test() {
        val result = mutableListOf("x")
        val ref = "a,b".toDelimitedMutableList(result)
        assertSame(result, ref)
        assertEquals(listOf("x", "a", "b"), result)

        // 空字符串不会向 result 追加内容
        val emptyResult = mutableListOf("x")
        "  ,  ".toDelimitedMutableList(emptyResult)
        assertEquals(listOf("x"), emptyResult)
    }

    @Test
    fun toDelimitedMutableList_accumulate_test() {
        val result = mutableListOf("x")
        "a,b".toDelimitedMutableList(result)
        "c,d".toDelimitedMutableList(result)
        assertEquals(listOf("x", "a", "b", "c", "d"), result)
    }

    // endregion

    // region toDelimitedMutableSet

    @Test
    fun toDelimitedMutableSet_default_result_test() {
        assertEquals(mutableSetOf("a", "b"), "a,b".toDelimitedMutableSet())
        assertEquals(mutableSetOf("a"), "a".toDelimitedMutableSet())
        assertEquals(mutableSetOf<String>(), "".toDelimitedMutableSet())
    }

    @Test
    fun toDelimitedMutableSet_with_result_test() {
        val result = mutableSetOf("x")
        val ref = "a,b".toDelimitedMutableSet(result)
        assertSame(result, ref)
        assertEquals(setOf("x", "a", "b"), result)

        // 重复项会被去重
        "a,a".toDelimitedMutableSet(result)
        assertEquals(setOf("x", "a", "b"), result)
    }

    @Test
    fun toDelimitedMutableSet_accumulate_test() {
        val result = mutableSetOf("x")
        "a,b".toDelimitedMutableSet(result)
        "b,c".toDelimitedMutableSet(result)
        assertEquals(setOf("x", "a", "b", "c"), result)
    }

    // endregion

    // region toDelimitedString

    @Test
    fun toDelimitedString_basic_test() {
        assertEquals("", emptyList<String>().toDelimitedString())
        assertEquals("a", listOf("a").toDelimitedString())
        assertEquals("a,b,c", listOf("a", "b", "c").toDelimitedString())
        assertEquals("a;b", listOf("a", "b").toDelimitedString(';'))
        assertEquals("a;b;c", listOf("a", "b", "c").toDelimitedString(';'))
    }

    @Test
    fun toDelimitedString_interface_consistency_test() {
        // 与标准库 joinToString(separator) 保持一致（空集合与单元素集合不会插入分隔符）
        val collections = listOf(
            emptyList(),
            listOf("a"),
            listOf("a", "b"),
            listOf("a", "b", "c"),
            listOf("a", "b", "c", "d"),
        )
        val delimiters = listOf(',', ';', ' ')
        for (c in collections) {
            for (d in delimiters) {
                assertEquals(c.joinToString(separator = d.toString()), c.toDelimitedString(d))
            }
        }
    }

    @Test
    fun toDelimitedString_round_trip_test() {
        // 与 toDelimitedList 构成往返
        assertEquals("a,b,c", "a,b,c".toDelimitedList().toDelimitedString())
        assertEquals("a,b", " a , b ".toDelimitedList().toDelimitedString())
        assertEquals("a;b", "a;b".toDelimitedList(';').toDelimitedString(';'))
    }

    // endregion
}
