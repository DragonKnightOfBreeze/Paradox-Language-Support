package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供对本地化表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等功能。
 *
 * @see ParadoxLocalisationExpressionElement
 */
@WithGameTypeEP
interface ParadoxLocalisationExpressionSupport {
    fun supports(element: ParadoxExpressionElement): Boolean

    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {

    }

    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): PsiElement? {
        return null
    }

    fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Collection<PsiElement> {
        return resolve(element, rangeInElement, expressionText).singleton.setOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        return null
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxLocalisationExpressionSupport>("icu.windea.pls.localisationExpressionSupport")

        //目前来看，这里暂不需要尝试避免SOE

        fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(element)) return@f
                if (!gameType.supportsByAnnotation(ep)) return@f
                ep.annotate(element, rangeInElement, expressionText, holder)
            }
        }

        fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): PsiElement? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(element)) return@f null
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val r = ep.resolve(element, rangeInElement, expressionText)
                r
            }
        }

        fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Collection<PsiElement> {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(element)) return@f null
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val r = ep.multiResolve(element, rangeInElement, expressionText).orNull()
                r
            }.orEmpty()
        }

        fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
            val gameType = selectGameType(element)
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(element)) return@f
                if (!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.getReferences(element, rangeInElement, expressionText).orNull()
                if (r != null) return r
            }
            return null
        }

        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
            val gameType by lazy { selectGameType(element) }
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(element)) return@f
                if (!gameType.supportsByAnnotation(ep)) return@f
                ep.complete(context, result)
            }
        }
    }
}
