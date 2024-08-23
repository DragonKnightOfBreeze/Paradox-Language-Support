package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptedVariableReference>() ?: return
        if(element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project
        
        ParadoxCompletionManager.initializeContext(parameters, context)
        
        //同时需要同时查找当前文件中的和全局的
        val selector = scriptedVariableSelector(project, element).contextSensitive().distinctByName()
        ParadoxLocalScriptedVariableSearch.search(selector).processQueryAsync { processScriptedVariable(context, result, it) }
        ParadoxGlobalScriptedVariableSearch.search(selector).processQueryAsync { processScriptedVariable(context, result, it) }
        
        ParadoxCompletionManager.completeExtendedScriptedVariable(context, result)
    }
    
    @Suppress("SameReturnValue")
    private fun processScriptedVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        ProgressManager.checkCanceled()
        val name = element.name ?: return true
        if(context.completionIds?.add(name) == false) return true //排除重复项
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(PlsIcons.Nodes.ScriptedVariable)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withCompletionId()
        result.addElement(lookupElement, context)
        return true
    }
}

