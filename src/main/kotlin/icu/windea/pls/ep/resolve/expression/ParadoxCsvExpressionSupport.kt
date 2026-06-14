package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对CSV表达式（列）的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 * 注意：相比 [ParadoxScriptExpressionSupport]，仅支持有限的 [CwtDataType]。
 *
 * @see ParadoxExpression
 * @see ParadoxExpressionElement
 * @see ParadoxScriptExpressionElement
 */
@WithGameTypeEP
interface ParadoxCsvExpressionSupport {
    fun supports(config: CwtValueConfig, configExpression: CwtDataExpression) : Boolean

    fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {

    }

    fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        return null
    }

    fun resolveAll(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): List<PsiElement> {
        return resolve(element, rangeInElement, expressionText, config).to.singletonListOrEmpty()
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionSupport>("icu.windea.pls.csvExpressionSupport")
    }
}
