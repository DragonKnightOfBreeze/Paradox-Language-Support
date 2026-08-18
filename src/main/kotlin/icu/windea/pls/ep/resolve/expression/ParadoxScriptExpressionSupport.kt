package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对脚本表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 * @see ParadoxExpressionElement
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxScriptExpressionSupport {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(dataType: CwtDataType): Boolean

    fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder) {
        // by default nothing
    }

    fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return null
    }

    fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        return resolve(element, text, rangeInExpression, config, role).to.singletonListOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        return emptyList()
    }

    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // by default nothing
    }

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
        @JvmField val CACHE = LazyValue<Map<CwtDataType, List<ParadoxScriptExpressionSupport>>>()

        fun get(dataType: CwtDataType): List<ParadoxScriptExpressionSupport> = CACHE.get()?.get(dataType).orEmpty()

        // region Implementations

        init {
            CACHE.initialize { computeCache() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { computeCache() } }
        }

        private fun computeCache(): Map<CwtDataType, List<ParadoxScriptExpressionSupport>> {
            val result = mutableMapOf<CwtDataType, List<ParadoxScriptExpressionSupport>>()
            val eps = EP_NAME.extensionList
            CwtDataType.entries.values.forEach { dataType -> eps.filterFast { ep -> ep.supports(dataType) }.orNull()?.let { result[dataType] = it.optimized() } }
            return result.optimized()
        }

        // endregion
    }
}
