package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

///**
// * 提供命令字段名字的代码补全。
// */
//class ParadoxLocalisationCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
//    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
//        val element = parameters.position.parent.castOrNull<ParadoxLocalisationCommandIdentifier>() ?: return
//        val offsetInParent = parameters.offset - element.startOffset
//        val keyword = element.getKeyword(offsetInParent)
//        val file = parameters.originalFile
//        val project = file.project
//        val gameType = file.fileInfo?.rootInfo?.gameType ?: return
//        val configGroup = getCwtConfig(project).get(gameType)
//        
//        context.put(PlsCompletionKeys.parametersKey, parameters)
//        context.put(PlsCompletionKeys.originalFileKey, file)
//        context.put(PlsCompletionKeys.contextElementKey, element)
//        context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
//        context.put(PlsCompletionKeys.keywordKey, keyword)
//        context.put(PlsCompletionKeys.configGroupKey, configGroup)
//        context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeHandler.getScopeContext(element))
//        
//        //提示scope
//        ParadoxConfigHandler.completeSystemScope(context, result)
//        ParadoxConfigHandler.completePredefinedLocalisationScope(context, result)
//        
//        //提示command
//        ParadoxConfigHandler.completePredefinedLocalisationCommand(context, result)
//        
//        //提示<scripted_loc>
//        ParadoxConfigHandler.completeScriptedLoc(context, result)
//        
//        //提示value[event_target]和value[global_event_target]
//        ParadoxConfigHandler.completeEventTarget(context, result)
//        
//        //提示value[variable]
//        ParadoxConfigHandler.completeVariable(context, result)
//    }
//}

