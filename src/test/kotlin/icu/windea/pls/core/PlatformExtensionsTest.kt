package icu.windea.pls.core

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.util.tupleOf
import org.junit.Assert
import org.junit.Test

class PlatformExtensionsTest {
    @Test
    fun unquoteTest() {
        // 空字符串 -> 空
        run {
            Assert.assertEquals("", "".unquote())
        }
        // 单个引号/仅引号对 -> 空
        run {
            Assert.assertEquals("", "\"".unquote())
            Assert.assertEquals("", "\"\"".unquote())
        }
        // 无首尾引号 -> 原样返回
        run {
            Assert.assertEquals("abc", "abc".unquote())
            Assert.assertEquals("a\"b", "a\"b".unquote()) // 内部转义不处理（无外层引号）
        }
        // 仅左/仅右引号 -> 去对应一侧
        run {
            Assert.assertEquals("abc", "\"abc".unquote())
            Assert.assertEquals("abc", "abc\"".unquote())
        }
        // 成对引号 -> 去除首尾
        run {
            Assert.assertEquals("abc", "\"abc\"".unquote())
        }
        // 内部转义引号反转义（存在外层引号时）
        run {
            Assert.assertEquals("a\"b\"c", "\"a\\\"b\\\"c\"".unquote())
            Assert.assertEquals("a\"b", "\"a\\\"b\"".unquote())
        }
        // 尾部引号被转义 -> 不视为右引号：保留原样（不去除转义）
        run {
            Assert.assertEquals("abc\\\"", "abc\\\"".unquote())
        }
        // 外层去除后，前缀为转义引号 -> 反转义后保留引号字符
        run {
            Assert.assertEquals("\"abc", "\"\\\"abc\"".unquote())
        }
        // 自定义引号字符（单引号）
        run {
            Assert.assertEquals("a'b", "'a\\'b'".unquote('\''))
        }
    }

    @Test
    fun replaceAndQuoteIfNecessaryTest() {
        // 覆盖全长替换 -> 直接按需要包裹引号
        run {
            Assert.assertEquals("def", TextRange.create(0, 3).replaceAndQuoteIfNecessary("abc", "def"))
            Assert.assertEquals("\"e\"", TextRange.create(0, 3).replaceAndQuoteIfNecessary("\"b\"", "\"e\""))
        }
        // 在外层引号内替换 -> 避免重复包裹
        run {
            Assert.assertEquals("\"dec\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "de"))
            Assert.assertEquals("\"d\\\"c\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "d\""))
        }
        // 在外层引号内替换包含空白的文本 -> 替换值需要引号但内部不重复包裹
        run {
            Assert.assertEquals("\"x ybc\"", TextRange.create(1, 2).replaceAndQuoteIfNecessary("\"abc\"", "x y"))
        }
        // 替换接近全长（length >= original.length - 1）-> 直接对 replacement 进行 quote 判断
        run {
            Assert.assertEquals("\"d e\"", TextRange.create(0, 2).replaceAndQuoteIfNecessary("abc", "d e"))
        }
    }

    @Test
    fun findKeywordsWithRangesTest() {
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
    fun mergeTextRangesTest() {
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
}
