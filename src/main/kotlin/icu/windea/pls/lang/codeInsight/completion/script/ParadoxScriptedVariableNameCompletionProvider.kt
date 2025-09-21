package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
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
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.notSamePosition
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量的名字的代码补全。
 */
class ParadoxScriptedVariableNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeScriptedVariableNames) return

        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptScriptedVariable>() ?: return
        if (element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project

        ParadoxCompletionManager.initializeContext(parameters, context)

        //这里不需要查找本地的封装变量（即当前文件中声明的封装变量）
        val selector = selector(project, element).scriptedVariable().contextSensitive().notSamePosition(element).distinctByName()
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processQueryAsync { processScriptedVariable(context, result, it) }

        ParadoxCompletionManager.completeExtendedScriptedVariable(context, result)
    }

    @Suppress("SameReturnValue")
    private fun processScriptedVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        //不自动插入后面的等号
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
