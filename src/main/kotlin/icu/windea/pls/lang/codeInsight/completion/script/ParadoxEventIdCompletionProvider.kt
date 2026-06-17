package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.icon
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 提供（位于事件声明中的）事件ID中的事件命名空间的代码补全。
 */
class ParadoxEventIdCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        if (keyword.contains('.')) return

        ParadoxCompletionManager.initializeContext(parameters, context)

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
