package icu.windea.pls.core

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.util.tupleOf
import org.junit.Assert
import org.junit.Test

class PlatformExtensionsTest {
    @Test
    fun findKeywordsWithTextRanges_test() {
        // 单一关键字
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)))
            val actual = "foo.bar.suffix".findKeywordsWithTextRanges(listOf("foo"))
            Assert.assertEquals(expected, actual)
        }
        // 多关键字，不重叠
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)), tupleOf("bar", TextRange.create(4, 7)))
            val actual = "foo.bar.suffix".findKeywordsWithTextRanges(listOf("foo", "bar"))
            Assert.assertEquals(expected, actual)
        }
        // 存在包含关系时优先选择更长关键字
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)), tupleOf("bar", TextRange.create(4, 7)), tupleOf("barbar", TextRange.create(8, 14)))
            val actual = "foo.bar.barbar".findKeywordsWithTextRanges(listOf("foo", "barbar", "bar"))
            Assert.assertEquals(expected, actual)
        }
        // 重复匹配（非重叠，按贪心前进）
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)), tupleOf("foo", TextRange.create(3, 6)))
            val actual = "foofoo".findKeywordsWithTextRanges(listOf("foo"))
            Assert.assertEquals(expected, actual)
        }
        // 重叠候选：优先长度更长的关键字，步进不重叠
        run {
            val expected = listOf(tupleOf("aa", TextRange.create(0, 2)), tupleOf("aa", TextRange.create(2, 4)))
            val actual = "aaaa".findKeywordsWithTextRanges(listOf("a", "aa"))
            Assert.assertEquals(expected, actual)
        }
        // 空关键字或空集合 -> 空结果
        run {
            val expected = emptyList<Any>()
            val actual = "sample".findKeywordsWithTextRanges(emptyList())
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun mergeTextRanges_test() {
        // 空输入
        run {
            val expected = emptyList<TextRange>()
            val actual = emptyList<TextRange>().mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 单个区间
        run {
            val expected = listOf(TextRange.create(1, 2))
            val actual = listOf(TextRange.create(1, 2)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 重叠合并
        run {
            val expected = listOf(TextRange.create(0, 5))
            val actual = listOf(TextRange.create(0, 3), TextRange.create(2, 5)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 相邻合并（end == start）
        run {
            val expected = listOf(TextRange.create(0, 5))
            val actual = listOf(TextRange.create(0, 3), TextRange.create(3, 5)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 嵌套区间
        run {
            val expected = listOf(TextRange.create(0, 10))
            val actual = listOf(TextRange.create(0, 10), TextRange.create(2, 5)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 不相交区间
        run {
            val expected = listOf(TextRange.create(0, 3), TextRange.create(5, 7))
            val actual = listOf(TextRange.create(0, 3), TextRange.create(5, 7)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 非排序输入 + 多次合并成一个
        run {
            val expected = listOf(TextRange.create(1, 7))
            val actual = listOf(TextRange.create(5, 7), TextRange.create(1, 3), TextRange.create(2, 6)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 重复区间
        run {
            val expected = listOf(TextRange.create(1, 3))
            val actual = listOf(TextRange.create(1, 3), TextRange.create(1, 3)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 零长度区间与相邻合并
        run {
            val expected = listOf(TextRange.create(3, 5))
            val actual = listOf(TextRange.create(3, 3), TextRange.create(3, 5)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
        // 链式相邻合并
        run {
            val expected = listOf(TextRange.create(0, 3))
            val actual = listOf(TextRange.create(0, 1), TextRange.create(1, 2), TextRange.create(2, 3)).mergeTextRanges()
            Assert.assertEquals(expected, actual)
        }
    }

    // region 其他纯逻辑扩展

    @Test
    fun mergeValue_test() {
        val ref = Ref.create<String?>(null)
        // newValue 为 null：不变，返回 true
        Assert.assertTrue(ref.mergeValue(null) { a, b -> a + b })
        Assert.assertNull(ref.get())
        // oldValue 为 null：直接设置
        Assert.assertTrue(ref.mergeValue("a") { a, b -> a + b })
        Assert.assertEquals("a", ref.get())
        // 两者都非空：合并
        Assert.assertTrue(ref.mergeValue("b") { a, b -> a + b })
        Assert.assertEquals("ab", ref.get())
        // 合并结果为 null：设置 null 并返回 false
        Assert.assertFalse(ref.mergeValue("c") { _, _ -> null })
        Assert.assertNull(ref.get())
    }

    @Test
    fun cancelable_test() {
        Assert.assertEquals(1, cancelable { 1 })
        // 普通异常原样抛出
        Assert.assertThrows(RuntimeException::class.java) { cancelable<Int> { throw RuntimeException("boom") } }
        // ProcessCanceledException 原样抛出
        Assert.assertThrows(ProcessCanceledException::class.java) { cancelable<Int> { throw ProcessCanceledException() } }
        // 根因为 PCE 的异常，抛出根因
        Assert.assertThrows(ProcessCanceledException::class.java) {
            cancelable<Int> { throw RuntimeException(ProcessCanceledException()) }
        }
    }

    @Test
    fun runCatchingCancelable_test() {
        Assert.assertEquals("ok", runCatchingCancelable { "ok" }.getOrThrow())
        Assert.assertTrue(runCatchingCancelable<String> { throw RuntimeException("boom") }.isFailure)
        // PCE 不会被包装为失败结果，而是直接抛出
        Assert.assertThrows(ProcessCanceledException::class.java) {
            runCatchingCancelable<String> { throw ProcessCanceledException() }
        }
    }

    @Test
    fun segmentContains_test() {
        // TextRange 实现了 Segment
        val range = TextRange.create(0, 5)
        Assert.assertTrue(range.contains(TextRange.create(1, 3)))
        Assert.assertTrue(range.contains(TextRange.create(0, 5)))
        Assert.assertFalse(range.contains(TextRange.create(0, 6)))
        Assert.assertFalse(range.contains(TextRange.create(3, 6)))
    }

    // endregion
}
