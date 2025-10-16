package icu.windea.pls.core

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.util.tupleOf
import org.junit.Assert
import org.junit.Test

class PlatformExtensionsTest {
    @Test
    fun replaceAndQuoteIfNecessaryTest() {
        Assert.assertEquals("def", TextRange.create(0, 3).replaceAndQuoteIfNecessary("abc", "def"))
        Assert.assertEquals("\"e\"", TextRange.create(0, 3).replaceAndQuoteIfNecessary("\"b\"", "\"e\""))
        Assert.assertEquals("\"dec\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "de"))
        Assert.assertEquals("\"d\\\"c\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "d\""))
    }

    @Test
    fun findKeywordsWithRangesTest() {
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)))
            val actual = "foo.bar.suffix".findKeywordsWithTextRanges(listOf("foo"))
            Assert.assertEquals(expected, actual)
        }
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)), tupleOf("bar", TextRange.create(4, 7)))
            val actual = "foo.bar.suffix".findKeywordsWithTextRanges(listOf("foo", "bar"))
            Assert.assertEquals(expected, actual)
        }
        run {
            val expected = listOf(tupleOf("foo", TextRange.create(0, 3)), tupleOf("bar", TextRange.create(4, 7)), tupleOf("barbar", TextRange.create(8, 14)))
            val actual = "foo.bar.barbar".findKeywordsWithTextRanges(listOf("foo", "barbar", "bar"))
            Assert.assertEquals(expected, actual)
        }
    }
}
