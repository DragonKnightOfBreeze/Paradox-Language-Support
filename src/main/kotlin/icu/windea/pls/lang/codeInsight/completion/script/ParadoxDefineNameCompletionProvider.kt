package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByDefineExpression
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 提供已有的定值的命名空间和变量的代码补全。
 */
class ParadoxDefineNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeDefineNames) return

        val file = parameters.originalFile
        if (!ParadoxDefineManager.isDefineFile(file)) return

        val project = file.project
        val element = parameters.position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return

        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)

        val memberElement = if (element is ParadoxScriptValue) element else element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val blockElement = element.parent
        when (blockElement) {
            // possible define namespace input
            is ParadoxScriptRootBlock -> {
                // property value must be null or a block
                if (memberElement is ParadoxScriptProperty && memberElement.propertyValue.let { it != null && it !is ParadoxScriptBlock }) return

                val selector = selector(project, element).define().contextSensitive()
                    .filterBy { it.name != keyword } // skip if name = input
                    .distinctByDefineExpression()
                ParadoxDefineNamespaceSearch.search(null, selector).processAsync {
                    processDefineNamespace(context, result, it)
                }
            }
            // possible define variable input
            is ParadoxScriptBlock -> {
                // parent property must be a top level property
                val parentPropertyElement = blockElement.parent?.castOrNull<ParadoxScriptProperty>() ?: return
                if (parentPropertyElement.parent !is ParadoxScriptRootBlock) return

                val namespace = parentPropertyElement.name
                val selector = selector(project, element).define().contextSensitive()
                    .filterBy { it.name != keyword } // skip if name = input
                    .distinctByDefineExpression()
                ParadoxDefineVariableSearch.search(namespace, null, selector).processAsync {
                    processDefineVariable(context, result, it)
                }
            }
        }
    }

    @Suppress("SameReturnValue")
    private fun processDefineNamespace(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
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
        // 不自动插入后面的等号
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
