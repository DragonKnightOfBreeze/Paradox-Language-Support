package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 提供对脚本表达式的支持。
 *
 * 用于实现代码高亮、引用解析、代码补全等语言功能。
 *
 * @see ParadoxScriptExpressionElement
 */
@WithGameTypeEP
interface ParadoxScriptExpressionSupport {
    fun supports(config: CwtConfig<*>, configExpression: CwtDataExpression): Boolean

    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {

    }

    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        return null
    }

    fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        return resolve(element, rangeInElement, expressionText, config, isKey, false).singleton.setOrEmpty()
    }

    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
        return null
    }

    fun complete(context: ProcessingContext, result: CompletionResultSet) {

    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxScriptExpressionSupport>("icu.windea.pls.scriptExpressionSupport")
    }
}
