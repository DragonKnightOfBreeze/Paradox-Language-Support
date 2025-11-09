package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.annotations.WithGameTypeEP

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
        return resolve(element, rangeInElement, expressionText, config).singleton.setOrEmpty()
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionSupport>("icu.windea.pls.csvExpressionSupport")
    }
}
