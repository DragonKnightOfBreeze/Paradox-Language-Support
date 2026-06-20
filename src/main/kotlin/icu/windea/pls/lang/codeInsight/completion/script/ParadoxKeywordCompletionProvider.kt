package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.lang.codeInsight.completion.ChronicleLookupElements
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addElements
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.STRING_TOKENS

/**
 * 提供关键字的代码补全（要求不在定义声明中提供）。
 */
class ParadoxKeywordCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(STRING_TOKENS).withParent(psiElement(ParadoxScriptString::class.java))

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val element = position.parent?.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isLeftQuoted()) return
        if (element.text.isParameterized()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        // 排除所在文件可能包含定义声明的情况
        val path = context.file.fileInfo?.path
        if (path != null && ParadoxConfigMatchService.getTypeConfigCandidates(CwtTypeConfigMatchContext(context.configGroup, path)).isNotEmpty()) return

        // 排除存在上下文规则的情况
        if (ParadoxConfigManager.getConfigContext(element)?.getConfigs().isNotNullOrEmpty()) return

        // 2.1.8 同样排除定值的脚本文件
        if (ParadoxDefineManager.isDefineFile(context.file)) return

        result.addElements(ChronicleLookupElements.keywordLookupElements, context)
    }
}
