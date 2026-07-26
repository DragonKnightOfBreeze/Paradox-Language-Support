package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxCompletionUtil {
    fun processScriptedVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name?.orNull() ?: return true
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.ScriptedVariable)
            .withScriptedVariablePresentableNames(element)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefinition(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return true
        val name = element.name.orNull() ?: return true // skip anonymous definitions
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
            .withPatchableTailText(context.patchableTailText)
            .withDefinitionPresentableNames(element)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineNamespace)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineVariable)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}
