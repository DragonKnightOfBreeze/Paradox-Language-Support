package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptedVariableReferenceCompletionProvider : ParadoxCompletionProvider() {
    private val insertHandler = InsertHandler<LookupElement> { context, _ ->
        // 因为只能在 `$...$` 引用中出现，如果后面没有 `$`，需要自动补充，并将光标移到补充 `$` 之前
        val editor = context.editor
        val caretModel = editor.caretModel
        val suffixChar = editor.document.charsSequence.getOrNull(caretModel.offset)
        if (suffixChar != '$') {
            EditorModificationUtil.insertStringAtCaret(editor, "$")
            caretModel.moveToOffset(caretModel.offset - 1)
        }
    }

    val elementPattern get() = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position
        val project = parameters.originalFile.project
        val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive().distinct()
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processAsync { processScriptedVariable(it, result) }
    }

    @Suppress("SameReturnValue")
    private fun processScriptedVariable(it: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
        ProgressManager.checkCanceled()
        val name = it.name ?: return true
        val icon = it.icon
        val typeFile = it.containingFile
        val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withInsertHandler(insertHandler)
        result.addElement(lookupElement)
        return true
    }
}

