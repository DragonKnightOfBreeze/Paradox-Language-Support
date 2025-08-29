package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.startOffset
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 用于在脚本文件中提供定义的上下文信息（如：`definitionKey = {`）。
 */
class ParadoxDefinitionDeclarationRangeHandler : DeclarationRangeHandler<ParadoxScriptProperty> {
    override fun getDeclarationRange(container: ParadoxScriptProperty): TextRange? {
        if (container.definitionInfo == null) return null
        val valueElement = container.propertyValue ?: return null
        val startOffset = container.propertyKey.startOffset
        val endOffset = when {
            valueElement is ParadoxScriptBlock -> valueElement.startOffset + 1 //包括" = {"
            else -> valueElement.startOffset
        }
        return TextRange.create(startOffset, endOffset)
    }
}
