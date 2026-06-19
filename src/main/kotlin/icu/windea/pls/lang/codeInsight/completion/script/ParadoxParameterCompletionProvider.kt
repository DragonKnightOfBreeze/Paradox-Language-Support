package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.PARAMETER_TOKENS

/**
 * 提供参数的名字的代码补全。
 */
class ParadoxParameterCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(PARAMETER_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        // 对于定义声明中的 `$PARAM$` 引用和 `[[PARAM]...]` 引用
        val tokenElement = parameters.position
        val element = tokenElement.parent

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxParameterManager.completeParameters(element, context, result)
    }
}
