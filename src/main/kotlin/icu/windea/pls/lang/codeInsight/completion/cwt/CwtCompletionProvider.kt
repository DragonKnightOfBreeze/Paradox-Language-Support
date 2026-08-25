package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionContext
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionFactory
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionManager
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult

/**
 * 提供 CWT 文件中的代码补全，同时包括普通文件和规则文件。
 */
class CwtCompletionProvider : CwtCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement = position.parent
        if (contextElement !is CwtOptionKey && contextElement !is CwtPropertyKey && contextElement !is CwtString) return

        val globalContext = GlobalCompletionContext.create(contextElement, parameters, context)
        val context = CwtCompletionContext.create(globalContext)

        // 3.0.2 只要能获取到规则分组（即使不是特定游戏类型的），就将当前文件视为规则文件，适用规则文件的代码补全逻辑
        CwtCompletionManager.addConfigCompletions(context, result)
        // 3.0.2 如果针对规则文件的代码补全没有任何候选项，再回退到针对普通文件的代码补全
        if (context.completionIds.isNotEmpty()) return

        // 作为回退，提示关键字
        if (context.contextElement is CwtString && !context.leftQuoted) {
            CwtCompletionFactory.forKeyword().addToResult(globalContext, result)
        }
    }
}
