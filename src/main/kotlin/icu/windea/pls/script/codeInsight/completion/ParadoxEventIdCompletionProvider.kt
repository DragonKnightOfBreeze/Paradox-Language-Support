package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

/**
 * 提供事件ID中事件命名空间的代码补全。
 */
class ParadoxEventIdCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isParameterized()) return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        if (keyword.contains('.')) return
        val event = element.findParentByPath("id", definitionType = ParadoxDefinitionTypes.Event) //不处理内联的情况
        if (event !is ParadoxScriptProperty) return

        ParadoxCompletionManager.initializeContext(parameters, context)

        //仅提示脚本文件中向上查找到的那个合法的事件命名空间
        val eventNamespace = ParadoxEventManager.getMatchedNamespace(event) ?: return //skip
        val name = eventNamespace.value ?: return
        val typeFile = eventNamespace.containingFile
        val lookupElement = LookupElementBuilder.create(eventNamespace, name)
            .withIcon(PlsIcons.Nodes.EventNamespace)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCompletionId()
        result.addElement(lookupElement, context)
    }
}
