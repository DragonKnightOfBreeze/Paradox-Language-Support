package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供对本地化表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 * @see ParadoxExpressionElement
 * @see ParadoxLocalisationExpressionElement
 */
interface ParadoxLocalisationExpressionSupport {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(element: ParadoxExpressionElement): Boolean

    fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, holder: AnnotationHolder) {
        // by default nothing
    }

    fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): PsiElement? {
        return null
    }

    fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): List<PsiElement> {
        return resolve(element, text, rangeInExpression).to.singletonListOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): List<PsiReference> {
        return emptyList()
    }

    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // by default nothing
    }

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxLocalisationExpressionSupport>("icu.windea.pls.localisationExpressionSupport")
        @JvmField val CACHE = LazyValue<List<ParadoxLocalisationExpressionSupport>>()

        fun getAll(): List<ParadoxLocalisationExpressionSupport> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.initialize { computeCache() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { computeCache() } }
        }

        private fun computeCache(): List<ParadoxLocalisationExpressionSupport> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
