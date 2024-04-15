package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段作用域的代码补全。
 */
class ParadoxLocalisationCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent.castOrNull<ParadoxLocalisationCommandIdentifier>() ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        
        context.initialize(parameters)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.scopeContext = ParadoxScopeHandler.getScopeContext(element)
        
        //提示scope
        ParadoxCompletionManager.completeSystemScope(context, result)
        ParadoxCompletionManager.completePredefinedLocalisationScope(context, result)
        
        //提示value[event_target]和value[global_event_target]
        ParadoxCompletionManager.completeEventTarget(context, result)
    }
}
