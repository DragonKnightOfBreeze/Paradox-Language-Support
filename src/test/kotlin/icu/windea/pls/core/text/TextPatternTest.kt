package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see TextPattern
 */
class TextPatternTest {
    // region Literal

    @Test
    fun literal_matches() {
        Assert.assertEquals(TextPatternResult.Empty, TextPattern.Literal("abc").matches("abc"))
    }

    @Test
    fun literal_matchesEmpty() {
        Assert.assertEquals(TextPatternResult.Empty, TextPattern.Literal("").matches(""))
    }

    @Test
    fun literal_notMatching() {
        Assert.assertNull(TextPattern.Literal("abc").matches("abd"))
        Assert.assertNull(TextPattern.Literal("abc").matches(""))
        Assert.assertNull(TextPattern.Literal("abc").matches("abcd"))
        Assert.assertNull(TextPattern.Literal("").matches("abc"))
    }

    // endregion

    // region WithPrefix

    @Test
    fun withPrefix_matches() {
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithPrefix("prefix-").matches("prefix-value"))
    }

    @Test
    fun withPrefix_matchesEmptyPrefix() {
        // 空前缀匹配任意文本，并返回文本本身
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithPrefix("").matches("value"))
    }

    @Test
    fun withPrefix_matchesFullText() {
        // 前缀等于完整文本时，返回空字符串
        Assert.assertEquals(TextPatternResult.Single(""), TextPattern.WithPrefix("value").matches("value"))
    }

    @Test
    fun withPrefix_notMatching() {
        val pattern = TextPattern.WithPrefix("prefix-")
        Assert.assertNull(pattern.matches("value"))
        Assert.assertNull(pattern.matches("prefix"))
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region WithSuffix

    @Test
    fun withSuffix_matches() {
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithSuffix("-suffix").matches("value-suffix"))
    }

    @Test
    fun withSuffix_matchesEmptySuffix() {
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithSuffix("").matches("value"))
    }

    @Test
    fun withSuffix_matchesFullText() {
        Assert.assertEquals(TextPatternResult.Single(""), TextPattern.WithSuffix("value").matches("value"))
    }

    @Test
    fun withSuffix_notMatching() {
        val pattern = TextPattern.WithSuffix("-suffix")
        Assert.assertNull(pattern.matches("value"))
        Assert.assertNull(pattern.matches("suffix"))
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region WithSurrounding

    @Test
    fun withSurrounding_matches() {
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithSurrounding("[", "]").matches("[value]"))
    }

    @Test
    fun withSurrounding_matchesEmptySurrounding() {
        Assert.assertEquals(TextPatternResult.Single("value"), TextPattern.WithSurrounding("", "").matches("value"))
    }

    @Test
    fun withSurrounding_matchesFullText() {
        Assert.assertEquals(TextPatternResult.Single(""), TextPattern.WithSurrounding("[", "]").matches("[]"))
    }

    @Test
    fun withSurrounding_notMatching() {
        val pattern = TextPattern.WithSurrounding("[", "]")
        Assert.assertNull(pattern.matches("value]")) // 缺少前缀
        Assert.assertNull(pattern.matches("[value")) // 缺少后缀
        Assert.assertNull(pattern.matches("value"))
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region Delimited

    @Test
    fun delimited_matches() {
        Assert.assertEquals(TextPatternResult.Pair("left", "right"), TextPattern.Delimited(":").matches("left:right"))
    }

    @Test
    fun delimited_matchesFirstOccurrence() {
        // 多个分隔符时，仅按首个分隔符进行拆分
        Assert.assertEquals(TextPatternResult.Pair("a", "b:c"), TextPattern.Delimited(":").matches("a:b:c"))
    }

    @Test
    fun delimited_matchesDelimiterAtBoundary() {
        Assert.assertEquals(TextPatternResult.Pair("", "value"), TextPattern.Delimited(":").matches(":value"))
        Assert.assertEquals(TextPatternResult.Pair("value", ""), TextPattern.Delimited(":").matches("value:"))
    }

    @Test
    fun delimited_emptyDelimiter() {
        // indexOf("") 返回 0，因此空分隔符会将整个文本置于右侧
        Assert.assertEquals(TextPatternResult.Pair("", "value"), TextPattern.Delimited("").matches("value"))
    }

    @Test
    fun delimited_notMatching() {
        Assert.assertNull(TextPattern.Delimited(":").matches("value"))
        Assert.assertNull(TextPattern.Delimited(":").matches(""))
    }

    // endregion

    // region DelimitedWithPrefix

    @Test
    fun delimitedWithPrefix_matches() {
        Assert.assertEquals(TextPatternResult.Pair("left", "right"), TextPattern.DelimitedWithPrefix(":", "prefix-").matches("prefix-left:right"))
    }

    @Test
    fun delimitedWithPrefix_notMatching() {
        val pattern = TextPattern.DelimitedWithPrefix(":", "prefix-")
        Assert.assertNull(pattern.matches("left:right")) // 缺少前缀
        Assert.assertNull(pattern.matches("prefix-leftright")) // 缺少分隔符
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region DelimitedWithSuffix

    @Test
    fun delimitedWithSuffix_matches() {
        Assert.assertEquals(TextPatternResult.Pair("left", "right"), TextPattern.DelimitedWithSuffix(":", "-suffix").matches("left:right-suffix"))
    }

    @Test
    fun delimitedWithSuffix_notMatching() {
        val pattern = TextPattern.DelimitedWithSuffix(":", "-suffix")
        Assert.assertNull(pattern.matches("left:right")) // 缺少后缀
        Assert.assertNull(pattern.matches("leftright-suffix")) // 缺少分隔符
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region DelimitedWithSurrounding

    @Test
    fun delimitedWithSurrounding_matches() {
        Assert.assertEquals(TextPatternResult.Pair("left", "right"), TextPattern.DelimitedWithSurrounding(":", "[", "]").matches("[left:right]"))
    }

    @Test
    fun delimitedWithSurrounding_notMatching() {
        val pattern = TextPattern.DelimitedWithSurrounding(":", "[", "]")
        Assert.assertNull(pattern.matches("left:right")) // 缺少前后缀
        Assert.assertNull(pattern.matches("[leftright]")) // 缺少分隔符
        Assert.assertNull(pattern.matches(""))
    }

    // endregion

    // region Comparator

    @Test
    fun comparator_ordersByPriority() {
        // 按优先级升序排列：Delimited(0) < WithSuffix(70) < WithPrefix(80) < WithSurrounding(90) < Literal(100)
        val literal = TextPattern.Literal("a")
        val surrounding = TextPattern.WithSurrounding("[", "]")
        val prefix = TextPattern.WithPrefix("a")
        val suffix = TextPattern.WithSuffix("a")
        val delimited = TextPattern.Delimited(":")
        val sorted = listOf(literal, delimited, suffix, surrounding, prefix).sortedWith(TextPattern.Comparator)
        Assert.assertEquals(listOf(delimited, suffix, prefix, surrounding, literal), sorted)
    }

    @Test
    fun comparator_orderStringTieBreak() {
        // 相同优先级时按 orderString 升序比较
        val a = TextPattern.WithPrefix("a")
        val b = TextPattern.WithPrefix("b")
        Assert.assertTrue(TextPattern.Comparator.compare(a, b) < 0)
        Assert.assertTrue(TextPattern.Comparator.compare(b, a) > 0)
        Assert.assertEquals(0, TextPattern.Comparator.compare(a, a))
    }

    // endregion
}
