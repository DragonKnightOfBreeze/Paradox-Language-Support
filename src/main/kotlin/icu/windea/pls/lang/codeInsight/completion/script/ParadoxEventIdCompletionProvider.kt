package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.icon
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS

/**
 * 提供（位于事件声明中的）事件ID中的事件命名空间的代码补全。
 */
object ParadoxEventIdCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        if (keyword.contains('.')) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        // 仅提示同文件中绑定的那些事件命名空间
        val event = ParadoxEventManipulationService.getEventDeclarationCandidateFromEventId(element) ?: return
        val boundEventNamespaces = ParadoxEventManipulationService.getBoundNamespaceDeclarationsFromEventDeclaration(event)
        if (boundEventNamespaces.isEmpty()) return

        for (eventNamespace in boundEventNamespaces) {
            val name = eventNamespace.value ?: continue
            val typeFile = eventNamespace.containingFile
            val lookupElement = LookupElementBuilder.create(eventNamespace, name)
                .withIcon(PlsIcons.Nodes.EventNamespace)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }
}
