package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forExpression
import icu.windea.pls.lang.codeInsight.completion.withScriptedVariableLocalizedNamesIfNecessary
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.SCRIPTED_VARIABLE_REFERENCE_TOKENS

object ParadoxScriptedVariableReferenceCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptedVariableReference>() ?: return
        if (element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project

        ParadoxCompletionManager.initializeContext(parameters, context)

        // 需要同时查找当前文件中的和全局的
        val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive().distinct()
        ParadoxScriptedVariableSearch.searchLocal(null, selector).processAsync { processScriptedVariable(context, result, it) }
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processAsync { processScriptedVariable(context, result, it) }

        ParadoxExtendedCompletionManager.completeExtendedScriptedVariable(context, result)
    }

    @Suppress("SameReturnValue")
    private fun processScriptedVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        ProgressManager.checkCanceled()
        val name = element.name ?: return true
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(PlsIcons.Nodes.ScriptedVariable)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withScriptedVariableLocalizedNamesIfNecessary(element)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}

