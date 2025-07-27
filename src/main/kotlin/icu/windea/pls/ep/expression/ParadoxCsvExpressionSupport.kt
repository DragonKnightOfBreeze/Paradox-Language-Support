package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*

/**
 * 提供对CSV表达式（列）的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等功能。
 *
 * @see ParadoxCsvExpressionElement
 */
@WithGameTypeEP
interface ParadoxCsvExpressionSupport {
    fun supports(config: CwtValueConfig): Boolean

    fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {

    }

    fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        return null
    }

    fun multiResolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
        return resolve(element, rangeInElement, expressionText, config).singleton().setOrEmpty()
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxCsvExpressionSupport>("icu.windea.pls.csvExpressionSupport")

        fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
            val gameType = config.configGroup.gameType
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(config)) return@f
                if (!gameType.supportsByAnnotation(ep)) return@f
                ep.annotate(element, rangeInElement, expressionText, holder, config)
            }
        }

        fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
            val gameType = config.configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(config)) return@f null
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val r = ep.resolve(element, rangeInElement, expressionText, config)
                r
            }
        }

        fun multiResolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
            val gameType = config.configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(config)) return@f null
                if (!gameType.supportsByAnnotation(ep)) return@f null
                val r = ep.multiResolve(element, rangeInElement, expressionText, config).orNull()
                r
            }.orEmpty()
        }

        fun complete(context: ProcessingContext, result: CompletionResultSet) {
            val config = context.config ?: return
            if (config !is CwtValueConfig) return
            val gameType = config.configGroup.gameType
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(config)) return@f
                if (!gameType.supportsByAnnotation(ep)) return@f
                ep.complete(context, result)
            }
        }
    }
}
