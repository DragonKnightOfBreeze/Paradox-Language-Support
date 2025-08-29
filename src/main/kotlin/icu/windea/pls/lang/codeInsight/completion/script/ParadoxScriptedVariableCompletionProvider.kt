package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.codeInsight.completion.withPatchableIcon
import icu.windea.pls.lang.codeInsight.completion.withScriptedVariableLocalizedNamesIfNecessary
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxLocalScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptedVariableReference>() ?: return
        if (element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project

        ParadoxCompletionManager.initializeContext(parameters, context)

        //同时需要同时查找当前文件中的和全局的
        val selector = selector(project, element).scriptedVariable().contextSensitive().distinctByName()
        ParadoxLocalScriptedVariableSearch.search(selector).processQueryAsync { processScriptedVariable(context, result, it) }
        ParadoxGlobalScriptedVariableSearch.search(selector).processQueryAsync { processScriptedVariable(context, result, it) }

        ParadoxCompletionManager.completeExtendedScriptedVariable(context, result)
    }

    @Suppress("SameReturnValue")
    private fun processScriptedVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        ProgressManager.checkCanceled()
        val name = element.name ?: return true
        val icon = PlsIcons.Nodes.ScriptedVariable
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(icon)
            .withScriptedVariableLocalizedNamesIfNecessary(element)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}

