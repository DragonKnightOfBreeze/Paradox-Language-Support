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
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.SCRIPTED_VARIABLE_REFERENCE_TOKENS

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableReferenceCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptedVariableReference>() ?: return
        if (element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        // 需要同时查找当前文件中的和全局的
        val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive().distinct()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).processAsync {
            ParadoxCompletionUtil.processScriptedVariable(context, result, it)
        }
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processAsync {
            ParadoxCompletionUtil.processScriptedVariable(context, result, it)
        }

        ParadoxExtendedCompletionManager.completeExtendedScriptedVariable(context, result)
    }
}

