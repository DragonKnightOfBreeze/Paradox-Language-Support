package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.forExpression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 提供已有的定值命名空间和定值变量的代码补全。
 */
class ParadoxDefineNameCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeDefineNames) return

        val file = parameters.originalFile
        if (!ParadoxDefineManager.isDefineFile(file)) return

        val element = parameters.position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        completeDefineName(context, result)
    }

    private fun completeDefineName(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val element = context.contextElement
        val memberElement = element as? ParadoxScriptValue ?: element.parent as? ParadoxScriptProperty ?: return
        val blockElement = element.parent
        when (blockElement) {
            // possible define namespace input
            is ParadoxScriptRootBlock -> {
                // property value must be null or a block
                if (memberElement is ParadoxScriptProperty && memberElement.propertyValue.let { it != null && it !is ParadoxScriptBlock }) return

                val selector = ParadoxDefineNamespaceSearch.selector(context.project, element).contextSensitive().distinct()
                    .filterBy { it.name != context.keyword } // skip if name = input
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
                val selector = ParadoxDefineVariableSearch.selector(context.project, element).contextSensitive().distinct()
                    .filterBy { it.name != context.keyword } // skip if name = input
                ParadoxDefineVariableSearch.search(namespace, null, selector).processAsync {
                    processDefineVariable(context, result, it)
                }
            }
        }
    }

    @Suppress("SameReturnValue")
    private fun processDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.DefineNamespace)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    @Suppress("SameReturnValue")
    private fun processDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.DefineVariable)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }
}
