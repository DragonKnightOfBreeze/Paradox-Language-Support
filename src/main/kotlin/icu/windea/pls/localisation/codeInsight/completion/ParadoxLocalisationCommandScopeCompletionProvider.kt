package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段作用域的代码补全。
 */
class ParadoxLocalisationCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent.castOrNull<ParadoxLocalisationCommandIdentifier>() ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        val file = parameters.originalFile
        val project = file.project
        val gameType = file.fileInfo?.rootInfo?.gameType ?: return
        val configGroup = getCwtConfig(project).get(gameType)
        
        context.parameters = parameters
        context.contextElement = element
        context.originalFile = file
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(element)
        
        //提示scope
        ParadoxConfigHandler.completeSystemScope(context, result)
        ParadoxConfigHandler.completePredefinedLocalisationScope(context, result)
        
        //提示value[event_target]和value[global_event_target]
        ParadoxConfigHandler.completeEventTarget(context, result)
    }
}
