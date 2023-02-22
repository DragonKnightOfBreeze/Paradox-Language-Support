package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量引用的名字的代码补全。
 */
class ParadoxScriptedVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        //同时需要同时查找当前文件中的和全局的
        val element = parameters.position.parent
        val file = parameters.originalFile
        val project = file.project
        val selector = scriptedVariableSelector(project, file).preferSameRoot().distinctByName()
        ParadoxLocalScriptedVariableSearch.search(element, selector).processQuery { processScriptedVariable(it, result) }
        ParadoxGlobalScriptedVariableSearch.search(selector).processQuery { processScriptedVariable(it, result) }
    }
    
    private fun processScriptedVariable(scriptedVariable: ParadoxScriptScriptedVariable, result: CompletionResultSet): Boolean {
        val name = scriptedVariable.name
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

