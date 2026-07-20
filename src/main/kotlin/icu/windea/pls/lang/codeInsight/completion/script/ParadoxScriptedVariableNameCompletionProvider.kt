package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionUtil
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.SCRIPTED_VARIABLE_NAME_TOKENS

/**
 * 提供已有的封装变量的名字的代码补全。
 */
class ParadoxScriptedVariableNameCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(SCRIPTED_VARIABLE_NAME_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeScriptedVariableNames) return

        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptScriptedVariableName>() ?: return
        if (element.text.isParameterized()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        // 这里不需要查找本地的封装变量（即当前文件中声明的封装变量）
        val selector = ParadoxScriptedVariableSearch.selector(context.project, element).contextSensitive().distinct()
            .filterBy { it.name != context.keyword } // skip if name = input
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processAsync {
            ParadoxCompletionUtil.processScriptedVariable(context, result, it)
        }

        ParadoxExtendedCompletionManager.completeExtendedScriptedVariable(context, result)
    }
}
