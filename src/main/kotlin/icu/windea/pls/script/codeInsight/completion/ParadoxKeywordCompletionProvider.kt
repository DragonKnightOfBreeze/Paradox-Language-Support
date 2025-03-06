package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

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

        //判断光标位置是否在定义声明中（这里的代码逻辑更加准确，兼容性更好）
        if (element.isExpression()) {
            val configContext = ParadoxExpressionManager.getConfigContext(element)
            if (configContext != null && configContext.isRootOrMember()) return
        }

        ParadoxCompletionManager.initializeContext(parameters, context)

        lookupElements.forEach { lookupElement ->
            result.addElement(lookupElement, context)
        }
    }
}
