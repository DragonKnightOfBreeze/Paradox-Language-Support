package icu.windea.pls.ep.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.text.TextPattern
import icu.windea.pls.core.text.TextPatternBasedBuilder
import icu.windea.pls.core.text.TextPatternBasedProvider
import icu.windea.pls.core.text.TextPatternMatchResult

abstract class CwtTextPatternBasedDataExpressionSupport : CwtDataExpressionSupport {
    protected data class Context(val type: CwtDataType, val action: CwtDataExpression.() -> Unit = {})

    private val providers = mutableListOf<TextPatternBasedProvider<Context, out TextPatternMatchResult>>()
    private val builder = TextPatternBasedBuilder<Context>(providers)

    init {
        registerProviders()
    }

    protected abstract fun registerProviders()

    protected fun fromLiteral(type: CwtDataType, value: String, action: CwtDataExpression.() -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(value)) { _, _ -> Context(type, action) }
    }

    protected fun fromParameterized(type: CwtDataType, prefix: String, suffix: String, action: CwtDataExpression.(String) -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, suffix)) { _, r -> Context(type) { action(r.value) } }
    }

    protected fun fromRanged(type: CwtDataType, prefix: String, action: CwtDataExpression.(String) -> Unit = {}) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, "")) { _, r -> if (isRangeLike(r.value)) Context(type) { action(r.value) } else null }
    }

    private fun isRangeLike(v: String): Boolean {
        return v.length >= 2 && v.first().let { c -> c == '[' || c == '(' } && v.last().let { c -> c == ']' || c == ')' }
    }

    final override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        val match = builder.build(expressionString) ?: return null
        return CwtDataExpression.create(expressionString, isKey, match.type).apply(match.action)
    }

    final override fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean {
        return providers.process { provider -> consumer.process(provider.pattern) }
    }
}
