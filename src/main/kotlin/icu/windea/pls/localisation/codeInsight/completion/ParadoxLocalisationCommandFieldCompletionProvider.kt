package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段名字的代码补全。
 */
class ParadoxLocalisationCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent.castOrNull<ParadoxLocalisationCommandIdentifier>() ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        val file = parameters.originalFile
        val project = file.project
        val gameType = file.fileInfo?.rootInfo?.gameType ?: return
        val configGroup = getConfigGroup(project, gameType)
        
        context.parameters = parameters
        context.contextElement = element
        context.originalFile = file
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(element)
        
        //提示scope
        CwtConfigHandler.completeSystemScope(context, result)
        CwtConfigHandler.completePredefinedLocalisationScope(context, result)
        
        //提示command
        CwtConfigHandler.completePredefinedLocalisationCommand(context, result)
        
        //提示<scripted_loc>
        CwtConfigHandler.completeScriptedLoc(context, result)
        
        //提示value[event_target]和value[global_event_target]
        CwtConfigHandler.completeEventTarget(context, result)
        
        //提示value[variable]
        CwtConfigHandler.completeVariable(context, result)
    }
}
