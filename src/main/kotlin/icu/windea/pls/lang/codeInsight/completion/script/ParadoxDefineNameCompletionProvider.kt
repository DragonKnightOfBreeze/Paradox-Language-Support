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
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByDefineExpression
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * 提供已有的定值的命名空间和变量的代码补全。
 */
class ParadoxDefineNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeDefineNames) return

        val file = parameters.originalFile
        if (!ParadoxDefineManager.isDefineFile(file)) return

        val project = file.project
        val position = parameters.position
        val keyElement = position.parent?.castOrNull<ParadoxScriptPropertyKey>() ?: return
        if (keyElement.text.isParameterized()) return

        val element = keyElement.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val defineInfo = element.defineInfo ?: return
        when (defineInfo) {
            is ParadoxDefineNamespaceInfo -> {
                val selector = selector(project, element).define().contextSensitive()
                    .filterBy { it.name != element.name } // 排除与正在输入的同名的
                    .distinctByDefineExpression()
                ParadoxDefineNamespaceSearch.search(defineInfo.namespace, selector).processAsync {
                    processDefineNamespace(context, result, it)
                }
            }
            is ParadoxDefineVariableInfo -> {
                val selector = selector(project, element).define().contextSensitive()
                    .filterBy { it.name != element.name } // 排除与正在输入的同名的
                    .distinctByDefineExpression()
                ParadoxDefineVariableSearch.search(defineInfo.namespace, defineInfo.variable, selector).processAsync {
                    processDefineVariable(context, result, it)
                }
            }
        }
    }

    @Suppress("SameReturnValue")
    private fun processDefineNamespace(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(PlsIcons.Nodes.DefineNamespace)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    @Suppress("SameReturnValue")
    private fun processDefineVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(PlsIcons.Nodes.DefineVariable)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .forScriptExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}
