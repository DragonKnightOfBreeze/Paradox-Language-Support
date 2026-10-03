package icu.windea.pls.config.match

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionMetadata
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import org.junit.Assert.*
import org.junit.Test

/**
 * [CwtTemplateMatchService] 的匹配逻辑测试。
 *
 * 用例覆盖：
 * - 完整模式（complete-mode）：常量片段完整匹配（忽略大小写）、动态片段接受任意文本（包括空字符串）、必须完整消费输入文本。
 * - 不完整模式（incomplete-mode）：优先完整匹配，否则允许常量片段仅匹配输入文本的前缀，否则忽略模板末尾尚未匹配的片段后继续尝试匹配。
 *
 * 测试直接构造 [CwtTemplateExpression] / [CwtDataExpression] 的轻量实现，因此无需依赖平台环境。
 *
 * @see CwtTemplateMatchService
 * @see CwtTemplateMatchResult
 * @see CwtTemplateMatchGroup
 */
class CwtTemplateMatchServiceTest {
    // region Test Helpers

    /** 常量片段。 */
    private fun c(value: String): CwtDataExpression = TestDataExpression(value, CwtDataTypes.Constant)

    /** 动态片段（引用片段）。 */
    private fun d(value: String): CwtDataExpression = TestDataExpression(value, CwtDataTypes.Value)

    private fun template(vararg snippets: CwtDataExpression): CwtTemplateExpression {
        return TestTemplateExpression(snippets.joinToString("") { it.expressionString }, snippets.toList())
    }

    /**
     * 校验匹配结果。期望值 [template] 使用紧凑表示：`c(value)` 表示常量片段，`d(value)` 表示动态片段，
     * `'text'@offset` 表示匹配到的文本及其偏移，末尾的 `!` 表示该片段为前缀匹配（不完整）。
     * 传入 `null` 表示期望匹配失败。
     *
     * 示例：
     * - 模板表达式 [template] - `job_<job>_add`
     * - 输入文本 [input] - `job_X_add`
     * - 期望值 [expected] - `c(job_)='job_'@0, d(<job>)='X'@4, c(_add)='_add'@5`
     */
    private fun assertMatch(
        template: CwtTemplateExpression,
        input: String,
        incomplete: Boolean,
        expected: String?,
    ) {
        val result = CwtTemplateMatchService.match(input, template, incomplete)
        assertEquals(expected, result.render())
        if (result != null) {
            assertEquals(input, result.value)
            // 结果级的不完整标记等同于“任意片段为前缀匹配”
            assertEquals(expected!!.contains('!'), result.incomplete)
        }
    }

    private fun CwtTemplateMatchResult?.render(): String? {
        if (this == null) return null
        return groups.joinToString(", ") { group ->
            val expression = when (group.expression.type) {
                CwtDataTypes.Constant -> "c(${group.expression.expressionString})"
                else -> "d(${group.expression.expressionString})"
            }
            val value = "'${group.value}'"
            val marker = if (group.incomplete) "!" else ""
            "$expression=$value@${group.offset}$marker"
        }
    }

    // endregion

    // region Complete Mode

    @Test
    fun testComplete_fullMatch() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "a_X_b", false, "c(a_)='a_'@0, d(value[foo])='X'@2, c(_b)='_b'@3")
    }

    @Test
    fun testComplete_emptyDynamic() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "a__b", false, "c(a_)='a_'@0, d(value[foo])=''@2, c(_b)='_b'@2")
    }

    @Test
    fun testComplete_ignoreCase() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "A_X_B", false, "c(a_)='A_'@0, d(value[foo])='X'@2, c(_b)='_B'@3")
    }

    @Test
    fun testComplete_trailingDynamic() {
        val t = template(c("a_"), d("value[foo]"))
        assertMatch(t, "a_X", false, "c(a_)='a_'@0, d(value[foo])='X'@2")
    }

    @Test
    fun testComplete_leadingDynamic() {
        val t = template(d("value[foo]"), c("_b"))
        assertMatch(t, "X_b", false, "d(value[foo])='X'@0, c(_b)='_b'@1")
    }

    @Test
    fun testComplete_leadingDynamic_emptyDynamic() {
        val t = template(d("value[foo]"), c("_b"))
        assertMatch(t, "_b", false, "d(value[foo])=''@0, c(_b)='_b'@0")
    }

    @Test
    fun testComplete_adjacentDynamics() {
        val t = template(c("a_"), d("value[foo]"), d("value[bar]"), c("_b"))
        assertMatch(t, "a_XY_b", false, "c(a_)='a_'@0, d(value[foo])='XY'@2, d(value[bar])=''@4, c(_b)='_b'@4")
    }

    @Test
    fun testComplete_notFullyConsumed_returnsNull() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "a_X_bc", false, null)
    }

    @Test
    fun testComplete_constantMismatch_returnsNull() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "b_X_b", false, null)
    }

    @Test
    fun testComplete_missingConstant_returnsNull() {
        val t = template(c("a_"), d("value[foo]"), c("_b"))
        assertMatch(t, "a_X", false, null)
    }

    @Test
    fun testComplete_dynamicOnly_emptyInput() {
        val t = template(d("value[foo]"))
        assertMatch(t, "", false, "d(value[foo])=''@0")
    }

    @Test
    fun testInvalidTemplate_returnsNull() {
        val t = TestTemplateExpression("", emptyList())
        assertMatch(t, "anything", false, null)
        assertMatch(t, "anything", true, null)
    }

    // endregion

    // region Incomplete Mode - constant prefix strategy

    @Test
    fun testIncomplete_constantPrefix_empty() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "", true, "c(any_country_in_)=''@0!")
    }

    @Test
    fun testIncomplete_constantPrefix_partial() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "any_", true, "c(any_country_in_)='any_'@0!")
    }

    @Test
    fun testIncomplete_constantPrefix_longerPartial() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "any_country_in", true, "c(any_country_in_)='any_country_in'@0!")
    }

    @Test
    fun testIncomplete_constantFull_thenEmptyDynamic() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "any_country_in_", true, "c(any_country_in_)='any_country_in_'@0, d(<geographic_region_short_key>)=''@15")
    }

    @Test
    fun testIncomplete_dynamicPartial() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "any_country_in_city_", true, "c(any_country_in_)='any_country_in_'@0, d(<geographic_region_short_key>)='city_'@15")
    }

    @Test
    fun testIncomplete_constantPrefix_noMatch_returnsNull() {
        val t = template(c("any_country_in_"), d("<geographic_region_short_key>"))
        assertMatch(t, "xyz", true, null)
    }

    @Test
    fun testIncomplete_leadingConstant_secondConstantPrefix() {
        val t = template(c("pre_"), c("mid_"), d("<x>"))
        assertMatch(t, "pre_mi", true, "c(pre_)='pre_'@0, c(mid_)='mi'@4!")
    }

    // endregion

    // region Incomplete Mode - ignore trailing snippets

    @Test
    fun testIncomplete_trailingConstant_stopsAtDynamic() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "job_", true, "c(job_)='job_'@0, d(<job>)=''@4")
    }

    @Test
    fun testIncomplete_trailingConstant_dynamicPartial() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "job_X", true, "c(job_)='job_'@0, d(<job>)='X'@4")
    }

    @Test
    fun testIncomplete_trailingConstant_fullMatch() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "job_X_add", true, "c(job_)='job_'@0, d(<job>)='X'@4, c(_add)='_add'@5")
    }

    @Test
    fun testIncomplete_trailingConstant_notFullyConsumed_returnsNull() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "job_X_add_more", true, null)
    }

    @Test
    fun testIncomplete_trailingConstant_prefixOfFirstConstant() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "job", true, "c(job_)='job'@0!")
    }

    @Test
    fun testIncomplete_trailingConstant_shortPrefixOfFirstConstant() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "j", true, "c(job_)='j'@0!")
    }

    @Test
    fun testIncomplete_trailingConstant_emptyInput() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "", true, "c(job_)=''@0!")
    }

    @Test
    fun testIncomplete_leadingDynamic_absorbsRemainder() {
        val t = template(d("value[foo]"), c("_b"))
        assertMatch(t, "X", true, "d(value[foo])='X'@0")
    }

    @Test
    fun testIncomplete_trailingConstant_noMatch_returnsNull() {
        val t = template(c("job_"), d("<job>"), c("_add"))
        assertMatch(t, "xyz", true, null)
    }

    // endregion

    // region Test Implementations

    private class TestDataExpression(
        override val expressionString: String,
        override val type: CwtDataType,
        override val role: CwtDataExpressionRole = CwtDataExpressionRole.Other,
    ) : CwtDataExpression {
        override val metadata: CwtDataExpressionMetadata get() = CwtDataExpressionMetadata.EMPTY
        override fun equals(other: Any?) = this === other || other is CwtDataExpression && expressionString == other.expressionString
        override fun hashCode() = expressionString.hashCode()
        override fun toString() = expressionString
    }

    private class TestTemplateExpression(
        override val expressionString: String,
        override val snippetExpressions: List<CwtDataExpression>,
    ) : CwtTemplateExpression {
        override val referenceExpressions: List<CwtDataExpression> = snippetExpressions.filter { it.type != CwtDataTypes.Constant }
        override fun equals(other: Any?) = this === other || other is CwtTemplateExpression && expressionString == other.expressionString
        override fun hashCode() = expressionString.hashCode()
        override fun toString() = expressionString
    }

    // endregion
}
