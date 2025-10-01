package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 提供关键字的代码补全（要求不在定义声明中提供）。
 */
class ParadoxKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val lookupElements = listOf(
        ParadoxCompletionManager.yesLookupElement,
        ParadoxCompletionManager.noLookupElement,
        ParadoxCompletionManager.blockLookupElement,
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isLeftQuoted()) return
        if (element.text.isParameterized()) return

        ParadoxCompletionManager.initializeContext(parameters, context)

        val configGroup = context.configGroup ?: return
        val path = parameters.originalFile.fileInfo?.path

        // 判断所在文件是否可能包含定义，如果可能，则不提示关键字
        val isDefinitionAwareFile = path != null && configGroup.types.values.any { CwtConfigManager.matchesFilePathPattern(it, path) }
        if(isDefinitionAwareFile) return

        lookupElements.forEach { lookupElement ->
            result.addElement(lookupElement, context)
        }
    }
}
