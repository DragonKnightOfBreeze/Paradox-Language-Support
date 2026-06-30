package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对CSV表达式（列）的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 * 注意：相比 [ParadoxScriptExpressionSupport]，仅支持有限的 [CwtDataType]。
 *
 * @see ParadoxExpressionElement
 * @see ParadoxScriptExpressionElement
 */
@WithGameTypeEP
interface ParadoxCsvExpressionSupport {
    fun supports(config: CwtValueConfig, configExpression: CwtDataExpression) : Boolean

    fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {

    }

    fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        return null
    }

    fun resolveAll(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): List<PsiElement> {
        return resolve(element, rangeInElement, text, config).to.singletonListOrEmpty()
    }

    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxCsvExpressionSupport>("icu.windea.pls.csvExpressionSupport")
    }
}
