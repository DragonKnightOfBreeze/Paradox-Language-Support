package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 定义的上下文信息（如：`definitionKey = {`）。
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
