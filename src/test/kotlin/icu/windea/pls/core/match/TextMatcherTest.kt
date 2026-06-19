package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class TextMatcherTest {
    // region matchesInt

    @Test
    fun matchesInt_basic() {
        Assert.assertTrue(TextMatcher.matchesInt("12345"))
        Assert.assertTrue(TextMatcher.matchesInt("0"))
        Assert.assertTrue(TextMatcher.matchesInt("000"))
        Assert.assertTrue(TextMatcher.matchesInt("+123"))
        Assert.assertTrue(TextMatcher.matchesInt("-456"))
    }

    @Test
    fun matchesInt_nonDigit() {
        Assert.assertFalse(TextMatcher.matchesInt("12a34"))
        Assert.assertFalse(TextMatcher.matchesInt("12 34"))
        Assert.assertFalse(TextMatcher.matchesInt("abc"))
        Assert.assertFalse(TextMatcher.matchesInt("12.34"))
    }

    @Test
    fun matchesInt_singleSign() {
        Assert.assertFalse(TextMatcher.matchesInt("+"))
        Assert.assertFalse(TextMatcher.matchesInt("-"))
        Assert.assertFalse(TextMatcher.matchesInt("+", leadingUnary = false))
        Assert.assertFalse(TextMatcher.matchesInt("-", leadingUnary = false))
    }

    @Test
    fun matchesInt_emptyRange() {
        Assert.assertFalse(TextMatcher.matchesInt(""))
        Assert.assertFalse(TextMatcher.matchesInt("abc", start = 0, end = 0))
    }

    @Test
    fun matchesInt_partialRange() {
        Assert.assertTrue(TextMatcher.matchesInt("abc123xyz", start = 3, end = 6))
        Assert.assertFalse(TextMatcher.matchesInt("abc123xyz", start = 0, end = 3))
    }

    @Test
    fun matchesInt_leadingUnaryEnabled() {
        Assert.assertTrue(TextMatcher.matchesInt("+0", leadingUnary = true))
        Assert.assertTrue(TextMatcher.matchesInt("-0", leadingUnary = true))
        Assert.assertTrue(TextMatcher.matchesInt("+123", leadingUnary = true))
        Assert.assertTrue(TextMatcher.matchesInt("-456", leadingUnary = true))
    }

    @Test
    fun matchesInt_leadingUnaryDisabled() {
        Assert.assertFalse(TextMatcher.matchesInt("+123", leadingUnary = false))
        Assert.assertFalse(TextMatcher.matchesInt("-456", leadingUnary = false))
        Assert.assertFalse(TextMatcher.matchesInt("+", leadingUnary = false))
        Assert.assertTrue(TextMatcher.matchesInt("123", leadingUnary = false))
    }

    // endregion

    // region matchesFloat

    @Test
    fun matchesFloat_basicLenient() {
        Assert.assertTrue(TextMatcher.matchesFloat("3.14"))
        Assert.assertTrue(TextMatcher.matchesFloat("0.0"))
        Assert.assertTrue(TextMatcher.matchesFloat("100.0"))
        Assert.assertTrue(TextMatcher.matchesFloat("+3.14"))
        Assert.assertTrue(TextMatcher.matchesFloat("-2.718"))
    }

    @Test
    fun matchesFloat_lenientEdgeCases() {
        // lenientDot = true（默认），允许 .5 / 5. / +.5 等
        Assert.assertTrue(TextMatcher.matchesFloat(".5", lenientDot = true))
        Assert.assertTrue(TextMatcher.matchesFloat("5.", lenientDot = true))
        Assert.assertTrue(TextMatcher.matchesFloat("+.5", lenientDot = true))
        Assert.assertTrue(TextMatcher.matchesFloat("-.5", lenientDot = true))
    }

    @Test
    fun matchesFloat_nonLenientEdgeCases() {
        // lenientDot = false，要求小数点前后各至少有一个数字
        Assert.assertFalse(TextMatcher.matchesFloat(".5", lenientDot = false))
        Assert.assertFalse(TextMatcher.matchesFloat("5.", lenientDot = false))
        Assert.assertFalse(TextMatcher.matchesFloat("+.5", lenientDot = false))
        Assert.assertFalse(TextMatcher.matchesFloat("-.5", lenientDot = false))
        Assert.assertFalse(TextMatcher.matchesFloat("+5.", lenientDot = false))
    }

    @Test
    fun matchesFloat_nonLenientValid() {
        // lenientDot = false，正常浮点数应通过
        Assert.assertTrue(TextMatcher.matchesFloat("3.14", lenientDot = false))
        Assert.assertTrue(TextMatcher.matchesFloat("+3.14", lenientDot = false))
        Assert.assertTrue(TextMatcher.matchesFloat("-2.7", lenientDot = false))
        Assert.assertTrue(TextMatcher.matchesFloat("0.0", lenientDot = false))
    }

    @Test
    fun matchesFloat_multipleDots() {
        // 多个小数点应失败
        Assert.assertFalse(TextMatcher.matchesFloat("1.2.3"))
        Assert.assertFalse(TextMatcher.matchesFloat("1..2"))
    }

    @Test
    fun matchesFloat_nonDigit() {
        Assert.assertFalse(TextMatcher.matchesFloat("1.2a"))
        Assert.assertFalse(TextMatcher.matchesFloat("a1.2"))
        Assert.assertFalse(TextMatcher.matchesFloat("abc"))
    }

    @Test
    fun matchesFloat_integerAsFloat() {
        // 没有小数点的整数在 lenient 和 non-lenient 模式下都应匹配
        Assert.assertTrue(TextMatcher.matchesFloat("123", lenientDot = true))
        Assert.assertTrue(TextMatcher.matchesFloat("123", lenientDot = false))
        Assert.assertTrue(TextMatcher.matchesFloat("+456", lenientDot = true))
        Assert.assertTrue(TextMatcher.matchesFloat("-789", lenientDot = false))
    }

    @Test
    fun matchesFloat_emptyRange() {
        Assert.assertFalse(TextMatcher.matchesFloat(""))
        Assert.assertFalse(TextMatcher.matchesFloat("abc", start = 0, end = 0))
    }

    @Test
    fun matchesFloat_singleSign() {
        Assert.assertFalse(TextMatcher.matchesFloat("+"))
        Assert.assertFalse(TextMatcher.matchesFloat("-"))
        Assert.assertFalse(TextMatcher.matchesFloat("+", leadingUnary = false))
        Assert.assertFalse(TextMatcher.matchesFloat("-", leadingUnary = false))
        Assert.assertFalse(TextMatcher.matchesFloat("."))
        Assert.assertFalse(TextMatcher.matchesFloat(".", leadingUnary = false))
    }

    @Test
    fun matchesFloat_partialRange() {
        Assert.assertTrue(TextMatcher.matchesFloat("abc3.14xyz", start = 3, end = 7))
        Assert.assertFalse(TextMatcher.matchesFloat("abc3.14xyz", start = 0, end = 3))
    }

    // endregion

    // region matchesPercentageField

    @Test
    fun matchesFloatPercentageField_basic() {
        Assert.assertTrue(TextMatcher.matchesFloatPercentageField("100%"))
        Assert.assertTrue(TextMatcher.matchesFloatPercentageField("0%"))
        Assert.assertTrue(TextMatcher.matchesFloatPercentageField("+50%"))
        Assert.assertTrue(TextMatcher.matchesFloatPercentageField("-25%"))
        Assert.assertTrue(TextMatcher.matchesFloatPercentageField("12.5%"))
    }

    @Test
    fun matchesFloatPercentageField_invalid() {
        Assert.assertFalse(TextMatcher.matchesFloatPercentageField("abc%"))
        Assert.assertFalse(TextMatcher.matchesFloatPercentageField("%"))
        Assert.assertFalse(TextMatcher.matchesFloatPercentageField("100"))
        Assert.assertFalse(TextMatcher.matchesFloatPercentageField("")) // 长度不足
    }

    // endregion

    // region matchesDateField

    @Test
    fun matchesDateField_defaultPattern() {
        Assert.assertTrue(TextMatcher.matchesDateField("2024.1.15"))
        Assert.assertTrue(TextMatcher.matchesDateField("1000.12.31"))
        Assert.assertTrue(TextMatcher.matchesDateField("1.1.1"))
    }

    @Test
    fun matchesDateField_invalidForDefaultPattern() {
        Assert.assertFalse(TextMatcher.matchesDateField("not.a.date"))
        Assert.assertFalse(TextMatcher.matchesDateField("2024-01-15"))
        Assert.assertFalse(TextMatcher.matchesDateField(""))
    }

    @Test
    fun matchesDateField_customPattern() {
        Assert.assertTrue(TextMatcher.matchesDateField("2024-01-15", "yyyy-MM-dd"))
        Assert.assertTrue(TextMatcher.matchesDateField("15.01.2024", "dd.MM.yyyy"))
        Assert.assertTrue(TextMatcher.matchesDateField("2024", "yyyy"))
    }

    @Test
    fun matchesDateField_customPatternInvalid() {
        Assert.assertFalse(TextMatcher.matchesDateField("2024.01.15", "yyyy-MM-dd"))
        Assert.assertFalse(TextMatcher.matchesDateField("abc", "yyyy"))
    }

    @Test
    fun matchesDateField_checkUnary() {
        Assert.assertTrue(TextMatcher.matchesDateField("2200.1.1", leadingUnary = false))
        Assert.assertTrue(TextMatcher.matchesDateField("2200.1.1", leadingUnary = true))
        Assert.assertFalse(TextMatcher.matchesDateField("-2200.1.1", leadingUnary = false))
        Assert.assertTrue(TextMatcher.matchesDateField("-2200.1.1", leadingUnary = true))
    }

    // endregion
}
