package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.PlsLookupElements
import icu.windea.pls.lang.codeInsight.completion.addElements
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 提供关键字的代码补全（要求不在定义声明中提供）。
 */
class ParadoxKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isLeftQuoted()) return
        if (element.text.isParameterized()) return

        ParadoxCompletionManager.initializeContext(parameters, context)

        val configGroup = context.configGroup ?: return
        val path = parameters.originalFile.fileInfo?.path

        // 排除所在文件可能包含定义声明的情况
        if (path != null && ParadoxConfigMatchService.getTypeConfigCandidates(CwtTypeConfigMatchContext(configGroup, path)).isNotEmpty()) return

        // 排除存在上下文规则的情况
        if (ParadoxConfigManager.getConfigContext(element)?.getConfigs().isNotNullOrEmpty()) return

        // 2.1.8 同样排除定值的脚本文件
        if (ParadoxDefineManager.isDefineFile(parameters.originalFile)) return

        result.addElements(PlsLookupElements.keywordLookupElements, context)
    }
}
