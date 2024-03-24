package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.core.*
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
        
        //同时需要同时查找当前文件中的和全局的
        val selector = scriptedVariableSelector(project, element).contextSensitive().distinctByName()
        ParadoxLocalScriptedVariableSearch.search(selector).processQuery { processScriptedVariable(it, result) }
        ParadoxGlobalScriptedVariableSearch.search(selector).processQuery { processScriptedVariable(it, result) }
    }
    
    @Suppress("SameReturnValue")
    private fun processScriptedVariable(scriptedVariable: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
        ProgressManager.checkCanceled()
        val name = scriptedVariable.name ?: return true
        val icon = scriptedVariable.icon
        val tailText = scriptedVariable.value?.let { " = $it" }
        val typeFile = scriptedVariable.containingFile
        val lookupElement = LookupElementBuilder.create(scriptedVariable, name).withIcon(icon)
            .withTailText(tailText)
            .withTypeText(typeFile.name, typeFile.icon, true)
        result.addElement(lookupElement)
        return true
    }
}

