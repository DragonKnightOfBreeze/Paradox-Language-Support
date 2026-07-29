package icu.windea.pls.ep.config.configExpression

import com.intellij.util.Processor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionMetadataBuilder
import icu.windea.pls.config.configExpression.CwtDataExpressionMetadataBuilderWithInput
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configExpression.acceptInput
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.text.TextPattern
import icu.windea.pls.core.text.TextPatternBasedBuilder
import icu.windea.pls.core.text.TextPatternBasedProvider
import icu.windea.pls.core.text.TextPatternMatchResult

abstract class CwtTextPatternBasedDataExpressionSupport : CwtDataExpressionSupport {
    private data class Context(val dataType: CwtDataType, val metadataBuilder: CwtDataExpressionMetadataBuilder? = null)

    private val providers = mutableListOf<TextPatternBasedProvider<Context, out TextPatternMatchResult>>()
    private val builder = TextPatternBasedBuilder(providers)

    init {
        registerProviders()
    }

    protected abstract fun registerProviders()

    protected fun fromLiteral(dataType: CwtDataType, value: String, metadataBuilder: CwtDataExpressionMetadataBuilder? = null) {
        providers += TextPatternBasedProvider(TextPattern.from(value)) { _, _ -> Context(dataType, metadataBuilder) }
    }

    protected fun fromParameterized(dataType: CwtDataType, prefix: String, suffix: String, metadataBuilder: CwtDataExpressionMetadataBuilderWithInput? = null) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, suffix)) { _, r -> Context(dataType, metadataBuilder?.acceptInput(r.value)) }
    }

    protected fun fromRanged(dataType: CwtDataType, prefix: String, metadataBuilder: CwtDataExpressionMetadataBuilderWithInput? = null) {
        providers += TextPatternBasedProvider(TextPattern.from(prefix, "")) { _, r -> if (isRangeLike(r.value)) Context(dataType, metadataBuilder?.acceptInput(r.value)) else null }
    }

    private fun isRangeLike(v: String): Boolean {
        return v.length >= 2 && v.first().let { c -> c == '[' || c == '(' } && v.last().let { c -> c == ']' || c == ')' }
    }

    final override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        val context = builder.build(expressionString) ?: return null
        return CwtDataExpression.create(expressionString, context.dataType, role, context.metadataBuilder)
    }

    final override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return resolve(expressionString, CwtDataExpressionRole.Other)
    }

    fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean {
        return providers.process { provider -> consumer.process(provider.pattern) }
    }
}

abstract class CwtPrefixBasedDataExpressionSupport : CwtDataExpressionSupport {
    private data class Provider(val dataType: CwtDataType, val prefix: String, val ignoreCase: Boolean)

    private val providers = mutableListOf<Provider>()

    init {
        registerProviders()
    }

    protected abstract fun registerProviders()

    protected fun from(dataType: CwtDataType, prefix: String, ignoreCase: Boolean) {
        providers += Provider(dataType, prefix, ignoreCase)
    }

    final override fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression? {
        providers.forEachFast f@{ provider ->
            val v = expressionString.removePrefixOrNull(provider.prefix) ?: return@f
            return CwtDataExpression.create(expressionString, provider.dataType, role) { value = v; ignoreCase = provider.ignoreCase }
        }
        return null
    }

    final override fun resolveTemplate(expressionString: String): CwtDataExpression? {
        return null // unsupported
    }
}
