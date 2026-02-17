package icu.windea.pls.ep.config.configExpression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.text.TextPattern
import icu.windea.pls.core.util.text.TextPatternBasedBuilder
import icu.windea.pls.core.util.text.TextPatternMatchResult

abstract class CwtTextPatternBasedDataExpressionResolver : CwtDataExpressionResolver {
    protected data class Match(
        val type: CwtDataType,
        val applyAction: CwtDataExpression.() -> Unit = {}
    )

    protected fun rule(
        type: CwtDataType,
        constant: String,
        applyAction: CwtDataExpression.() -> Unit = {}
    ): TextPatternBasedBuilder.Rule<Match, TextPatternMatchResult.Empty> {
        return TextPatternBasedBuilder.Rule(TextPattern.from(constant)) { _, _ -> Match(type, applyAction) }
    }

    protected fun rule(
        type: CwtDataType,
        prefix: String,
        suffix: String,
        applyAction: CwtDataExpression.(String) -> Unit = {}
    ): TextPatternBasedBuilder.Rule<Match, TextPatternMatchResult.Single> {
        return TextPatternBasedBuilder.Rule(TextPattern.from(prefix, suffix)) { _, r -> Match(type) { applyAction(r.value) } }
    }

    protected abstract val rules: List<TextPatternBasedBuilder.Rule<Match, out TextPatternMatchResult>>

    private val builder by lazy { TextPatternBasedBuilder<Match>(rules) }

    final override fun getTextPatterns(): List<TextPattern<*>> {
        return rules.map { it.pattern }
    }

    final override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        val match = builder.build(expressionString) ?: return null
        return CwtDataExpression.create(expressionString, isKey, match.type).apply(match.applyAction)
    }
}
