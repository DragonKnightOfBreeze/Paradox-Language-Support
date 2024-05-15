package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量的名字的代码补全。
 */
class ParadoxScriptedVariableNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeScriptedVariableNames) return
        
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptScriptedVariable>() ?: return
        if(element.text.isParameterized()) return
        val file = parameters.originalFile
        val project = file.project
        
        context.initialize(parameters)
        
        //这里不需要查找本地的封装变量（即当前文件中声明的封装变量）
        val selector = scriptedVariableSelector(project, element).contextSensitive().notSamePosition(element).distinctByName()
        ParadoxGlobalScriptedVariableSearch.search(selector).processQueryAsync { processScriptedVariable(context, result, it) }
        
        ParadoxCompletionManager.completeExtendedScriptedVariable(context, result)
    }
    
    @Suppress("SameReturnValue")
    private fun processScriptedVariable(context: ProcessingContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        //不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name ?: return true
        if(context.completionIds?.add(name) == false) return true //排除重复项
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(PlsIcons.Nodes.ScriptedVariable)
            .withTailText(tailText)
            .withTypeText(typeFile.name, typeFile.icon, true)
        result.addElement(lookupElement)
        return true
    }
}