package icu.windea.pls.lang.util

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.isParameterAwareIdentifier
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxExpressionManager
 */
class ParadoxExpressionManagerTest {
    // region isParameterized

    @Test
    fun isParameterized_dollarSyntax() {
        // 基本 $...$ 参数语法
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$abc$"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("aaa\$abc\$bbb"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("x\$y$"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$y\$x"))
        // 简短形式
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("$$"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$a$"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("x$\$y"))
    }

    @Test
    fun isParameterized_bracketSyntax() {
        // 基本 [[...]...] 参数语法
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("[[a]]"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("aaa[[a]]bbb"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("[[a]b]"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("x[[a]b]y"))
    }

    @Test
    fun isParameterized_notParameterized() {
        // 普通字符串，不含参数语法
        Assert.assertFalse(ParadoxExpressionManager.isParameterized(""))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("abc"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("hello_world"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("123"))
        // 长度不足
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("a"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("$"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("["))
    }

    @Test
    fun isParameterized_escaped() {
        // 转义后的 $ 或 [[ 不会被识别为参数
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\\\$a"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\\[[a"))
        // 检查所有出现的标记，即使之前已经存在被转义的标记
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\\\$abc$"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\\[[a]]"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\\$$"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\\\$abc\$def$"))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\\[[a]]b]]"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\\[[a]][[b]]]]"))
    }

    @Test
    fun isParameterized_conditionBlockFalse() {
        // conditionBlock = false 时不检测 [[...]] 语法
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$abc$", conditionBlock = false))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("[[a]]", conditionBlock = false))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("aaa[[a]]bbb", conditionBlock = false))
        // $ 语法仍正常检测
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("aaa\$abc\$bbb", conditionBlock = false))
    }

    @Test
    fun isParameterized_full() {
        // full = true: 整个字符串必须是 $...$ 形式
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$abc$", full = true))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("$$", full = true))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$a$", full = true))
        // 前后有多余字符 → false
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("", full = true))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("abc", full = true))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("aaa\$abc\$bbb", full = true))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("x\$y\$z\$w$", full = true))
        // 转义导致不匹配
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\$abc\\$", full = true))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\\\$abc$", full = true))
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("\$abc\$def\$gh$", full = true))
        // 多个 $ 对 → false（第二个 $ 不是 lastIndex）
        Assert.assertFalse(ParadoxExpressionManager.isParameterized("$$$$", full = true))
    }

    @Test
    fun isParameterized_edgeCases() {
        // 转义后的 \\ 不影响检测
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\\\\\$abc$"))
        // 仅含 [[ 但无 $$
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("[[a"))
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("[[[a]]]"))
        // 混合 $ 和 [[
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("a\$b\$c[[d]e]"))
        // 未闭合参数（仅第一个 $ 检测足以返回 true）
        Assert.assertTrue(ParadoxExpressionManager.isParameterized("\$abc"))
    }

    // endregion

    // region isParameterAwareIdentifier

    @Test
    fun isParameterAwareIdentifier_basic() {
        Assert.assertTrue("\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("aaa\$abc\$bbb".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]".isParameterAwareIdentifier())
        Assert.assertTrue("aaa[[a]]bbb".isParameterAwareIdentifier())
        Assert.assertTrue("aaa\$abc\$bbb[[c]]ccc".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_empty() {
        Assert.assertFalse("".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_plainIdentifier() {
        Assert.assertTrue("abc".isParameterAwareIdentifier())
        Assert.assertTrue("hello_world".isParameterAwareIdentifier())
        Assert.assertFalse("a@b".isParameterAwareIdentifier())
        Assert.assertFalse("a b".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_multipleParameters() {
        // 连续参数
        Assert.assertTrue("\$a$\$b$".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]][[b]]".isParameterAwareIdentifier())
        Assert.assertTrue("\$a$[[b]]".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]\$b$".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_nestedParameters() {
        // 深度嵌套括号
        Assert.assertTrue("[[a[[b]]c]]".isParameterAwareIdentifier())
        Assert.assertTrue("[[a[[b[[c]]d]]e]]".isParameterAwareIdentifier())
        // $ 嵌套在 [[...]] 中
        Assert.assertTrue("[[a\$b\$c]]".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_escaped() {
        // 反斜线转义的参数语法不被识别为参数
        Assert.assertFalse("\\\$abc$".isParameterAwareIdentifier())
        Assert.assertFalse("\\[[a]]".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_incomplete() {
        // 未闭合的 $...
        Assert.assertTrue("\$abc".isParameterAwareIdentifier())
        // 未闭合的 [[...
        Assert.assertTrue("[[a".isParameterAwareIdentifier())
        // 空参数
        Assert.assertTrue("$$".isParameterAwareIdentifier())
        Assert.assertTrue("[[ ]]".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_extraChars() {
        Assert.assertTrue("a.b".isParameterAwareIdentifier("."))
        Assert.assertTrue("\$abc$.b".isParameterAwareIdentifier("."))
        Assert.assertTrue("a[[b]].c".isParameterAwareIdentifier("."))
        Assert.assertFalse("a!b".isParameterAwareIdentifier("."))
    }

    @Test
    fun isParameterAwareIdentifier_edgePositions() {
        // 参数在开头/结尾
        Assert.assertTrue("\$abc\$xxx".isParameterAwareIdentifier())
        Assert.assertTrue("xxx\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]xxx".isParameterAwareIdentifier())
        Assert.assertTrue("xxx[[a]]".isParameterAwareIdentifier())
        // 仅包含参数
        Assert.assertTrue("\$abc$".isParameterAwareIdentifier())
        Assert.assertTrue("[[a]]".isParameterAwareIdentifier())
    }

    @Test
    fun isParameterAwareIdentifier_singleSpecialChars() {
        Assert.assertTrue("$".isParameterAwareIdentifier()) // `$` 自身是有效的标识符字符
        Assert.assertFalse("[".isParameterAwareIdentifier())
        Assert.assertFalse("]".isParameterAwareIdentifier())
    }

    // endregion

    // region getParameterRanges

    @Test
    fun getParameterRanges_dollarSyntax() {
        Assert.assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("\$abc$"))
        Assert.assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa\$abc\$bbb"))
        Assert.assertEquals(listOf(TextRange.create(0, 2)), ParadoxExpressionManager.getParameterRanges("$$"))
        Assert.assertEquals(listOf(TextRange.create(0, 3)), ParadoxExpressionManager.getParameterRanges("\$a$"))
    }

    @Test
    fun getParameterRanges_bracketSyntax() {
        Assert.assertEquals(listOf(TextRange.create(0, 5)), ParadoxExpressionManager.getParameterRanges("[[a]]"))
        Assert.assertEquals(listOf(TextRange.create(3, 8)), ParadoxExpressionManager.getParameterRanges("aaa[[a]]bbb"))
        Assert.assertEquals(listOf(TextRange.create(0, 6)), ParadoxExpressionManager.getParameterRanges("[[a]b]"))
        Assert.assertEquals(listOf(TextRange.create(1, 7)), ParadoxExpressionManager.getParameterRanges("x[[a]b]y"))
    }

    @Test
    fun getParameterRanges_mixed() {
        // $ 和 [[ 混合
        Assert.assertEquals(
            listOf(TextRange.create(1, 4), TextRange.create(5, 13)),
            ParadoxExpressionManager.getParameterRanges("a\$a\$a[[a]\$b$]bbb")
        )
    }

    @Test
    fun getParameterRanges_nested() {
        // 深度嵌套的括号
        Assert.assertEquals(
            listOf(TextRange.create(0, 11)),
            ParadoxExpressionManager.getParameterRanges("[[a[[b]]c]]")
        )
        Assert.assertEquals(
            listOf(TextRange.create(0, 17)),
            ParadoxExpressionManager.getParameterRanges("[[a[[b[[c]]d]]e]]")
        )
        // $ 嵌套在 [[ ]] 中
        Assert.assertEquals(
            listOf(TextRange.create(0, 9)),
            ParadoxExpressionManager.getParameterRanges("[[a\$b\$c]]")
        )
    }

    @Test
    fun getParameterRanges_escaped() {
        // 转义后的 $ 不参与解析
        Assert.assertEquals(
            listOf(TextRange.create(5, 8)),
            ParadoxExpressionManager.getParameterRanges("\\\$abc\$d$")
        )
        // 转义后的 [[ 不参与解析
        Assert.assertEquals(
            listOf<TextRange>(),
            ParadoxExpressionManager.getParameterRanges("\\[[a]]")
        )
    }

    @Test
    fun getParameterRanges_incomplete() {
        // 未闭合的 $... → 范围延伸到字符串末尾
        Assert.assertEquals(
            listOf(TextRange.create(0, 4)),
            ParadoxExpressionManager.getParameterRanges("\$abc")
        )
        // 未闭合的 [[... → 范围延伸到字符串末尾
        Assert.assertEquals(
            listOf(TextRange.create(0, 3)),
            ParadoxExpressionManager.getParameterRanges("[[a")
        )
        // 仅一个 [
        Assert.assertEquals(
            listOf<TextRange>(),
            ParadoxExpressionManager.getParameterRanges("[a")
        )
    }

    @Test
    fun getParameterRanges_multiple() {
        // 多个连续 $ 参数
        Assert.assertEquals(
            listOf(TextRange.create(0, 2), TextRange.create(2, 4)),
            ParadoxExpressionManager.getParameterRanges("$$$$")
        )
        // 多个连续 [[ 参数
        Assert.assertEquals(
            listOf(TextRange.create(0, 5), TextRange.create(5, 10)),
            ParadoxExpressionManager.getParameterRanges("[[a]][[b]]")
        )
    }

    @Test
    fun getParameterRanges_conditionBlockFalse() {
        Assert.assertEquals(
            listOf(TextRange.create(0, 3)),
            ParadoxExpressionManager.getParameterRanges("\$a$[[b]]", conditionBlock = false)
        )
        Assert.assertEquals(
            listOf<TextRange>(),
            ParadoxExpressionManager.getParameterRanges("[[a]]", conditionBlock = false)
        )
    }

    @Test
    fun getParameterRanges_noParameter() {
        Assert.assertEquals(listOf<TextRange>(), ParadoxExpressionManager.getParameterRanges(""))
        Assert.assertEquals(listOf<TextRange>(), ParadoxExpressionManager.getParameterRanges("abc"))
        Assert.assertEquals(listOf<TextRange>(), ParadoxExpressionManager.getParameterRanges("123"))
    }

    // endregion

    // region toRegex

    @Test
    fun toRegex_dollarSyntax() {
        val r1 = ParadoxExpressionManager.toRegex("a\$b\$c")
        Assert.assertTrue(r1.matches("ac"))
        Assert.assertTrue(r1.matches("abc"))
        Assert.assertTrue(r1.matches("abbc"))
        Assert.assertTrue(r1.matches("a123c"))
        Assert.assertFalse(r1.matches("a"))
        Assert.assertFalse(r1.matches("c"))
        Assert.assertFalse(r1.matches("acb"))

        // 仅参数 → .*
        val r2 = ParadoxExpressionManager.toRegex("\$a$")
        Assert.assertTrue(r2.matches(""))
        Assert.assertTrue(r2.matches("anything"))

        // 多个参数
        val r3 = ParadoxExpressionManager.toRegex("a\$b\$c\$d\$e")
        Assert.assertFalse(r3.matches("ae"))
        Assert.assertTrue(r3.matches("ace"))
        Assert.assertTrue(r3.matches("abcde"))
        Assert.assertFalse(r3.matches("aXXXXXe"))
        Assert.assertTrue(r3.matches("aXXXcXXe"))
    }

    @Test
    fun toRegex_bracketSyntax() {
        val r1 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]e]")
        Assert.assertTrue(r1.matches("abc"))
        Assert.assertTrue(r1.matches("abce"))
        Assert.assertFalse(r1.matches("abcd"))

        val r2 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e$]")
        Assert.assertTrue(r2.matches("abc"))
        Assert.assertTrue(r2.matches("abce"))
        Assert.assertTrue(r2.matches("abcd"))

        val r3 = ParadoxExpressionManager.toRegex("a\$b\$c[[d]\$e\$f]")
        Assert.assertTrue(r3.matches("abcf"))
        Assert.assertTrue(r3.matches("abcef"))
        Assert.assertTrue(r3.matches("abcdf"))

        // 无 $ 仅有 [[ ]]
        val r4 = ParadoxExpressionManager.toRegex("[[a]b]")
        Assert.assertTrue(r4.matches(""))
        Assert.assertTrue(r4.matches("b"))
        Assert.assertFalse(r4.matches("a"))
        Assert.assertFalse(r4.matches("ab"))
    }

    @Test
    fun toRegex_empty() {
        val r = ParadoxExpressionManager.toRegex("")
        Assert.assertTrue(r.matches(""))
        Assert.assertFalse(r.matches("a"))
    }

    @Test
    fun toRegex_noParameter() {
        val r = ParadoxExpressionManager.toRegex("hello")
        Assert.assertTrue(r.matches("hello"))
        Assert.assertFalse(r.matches("hell"))
        Assert.assertFalse(r.matches("helloo"))
    }

    @Test
    fun toRegex_conditionBlockFalse() {
        val r = ParadoxExpressionManager.toRegex("a\$b\$c[[d]e]", conditionBlock = false)
        // [[...]] 不被处理，$...$ 正常处理
        Assert.assertTrue(r.matches("ac[[d]e]"))
        Assert.assertTrue(r.matches("abc[[d]e]"))
        Assert.assertFalse(r.matches("abc"))
        Assert.assertFalse(r.matches("abce"))
    }

    @Test
    fun toRegex_singleParameter() {
        val r1 = ParadoxExpressionManager.toRegex("\$a$")
        Assert.assertTrue(r1.matches(""))
        Assert.assertTrue(r1.matches("anything"))
    }

    @Test
    fun toRegex_onlyBracket() {
        val r = ParadoxExpressionManager.toRegex("[[a]]")
        Assert.assertTrue(r.matches(""))
        Assert.assertFalse(r.matches("]"))
        Assert.assertFalse(r.matches("a"))
    }

    // endregion
}
