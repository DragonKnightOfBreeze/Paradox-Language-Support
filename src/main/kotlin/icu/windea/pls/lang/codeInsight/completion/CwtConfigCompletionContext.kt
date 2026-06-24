package icu.windea.pls.lang.codeInsight.completion

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.config.util.CwtConfigSchemaManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalBasedCompletionContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.psi.CwtBlockElement
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.cwt.psi.isOptionBlockValue
import icu.windea.pls.cwt.psi.isOptionValue
import icu.windea.pls.cwt.psi.isPropertyValue

data class CwtConfigCompletionContext(
    override val globalContext: GlobalCompletionContext,
    val configGroup: CwtConfigGroup,
    override val keyword: String,
    val expressionElement: CwtExpressionElement? = null,
    val containerElement: PsiElement? = null,
    val keyToMatch: String? = null,
    val optionContainerIdToMatch: String? = null,
    val schema: CwtSchemaConfig? = null,
    val contextConfigs: List<CwtMemberConfig<*>> = emptyList(),
    val isOptionKey: Boolean = false,
    val isOptionValue: Boolean = false,
    val isOptionBlockValue: Boolean = false,
    val inOption: Boolean = false,
    val isPropertyKey: Boolean = false,
    val isPropertyValue: Boolean = false,
    val isBlockValue: Boolean = false,
    val isKey: Boolean = false,
    val isKeyOnly: Boolean = false,
    val isValueOnly: Boolean = false,
) : GlobalBasedCompletionContext() {
    companion object {
        @JvmStatic
        fun create(globalContext: GlobalCompletionContext): CwtConfigCompletionContext? {
            return CwtConfigCompletionContextBuilder.build(globalContext)
        }
    }
}

// region Implementations

private object CwtConfigCompletionContextBuilder {
    fun build(globalContext: GlobalCompletionContext): CwtConfigCompletionContext? {
        val contextElement = globalContext.contextElement

        val configGroup = CwtConfigManager.getContainingConfigGroup(globalContext.file) ?: return null
        val keyword = globalContext.contextElement.getKeyword(globalContext.offsetInParent)
        val expressionElement = getExpressionElement(globalContext.contextElement) ?: return null
        val containerElement = getContainerElement(expressionElement) ?: return null
        val keyToMatch = getKeyToMatch(globalContext.contextElement)
        val optionContainerIdToMatch = getOptionContainerIdToMatch(expressionElement)

        val schema = configGroup.schemas.firstOrNull() ?: return null
        val contextConfigs = CwtConfigSchemaManager.getContextConfigs(expressionElement, containerElement, globalContext.file, schema)

        val isOptionKey = contextElement is CwtOptionKey || (contextElement is CwtString && contextElement.isOptionBlockValue())
        val isOptionBlockValue = contextElement is CwtString && contextElement.isOptionBlockValue()
        val isOptionValue = contextElement is CwtString && contextElement.isOptionValue()
        val inOption = isOptionKey || isOptionBlockValue || isOptionValue

        val isPropertyKey = expressionElement is CwtPropertyKey || expressionElement is CwtString && expressionElement.isBlockValue()
        val isBlockValue = expressionElement is CwtString && expressionElement.isBlockValue()
        val isPropertyValue = expressionElement is CwtString && expressionElement.isPropertyValue()

        val isKey = if (inOption) isOptionKey else isPropertyKey
        val isKeyOnly = contextElement is CwtOptionKey || contextElement is CwtPropertyKey
        val isValueOnly = contextElement is CwtString && if (inOption) !isOptionKey else !isPropertyKey

        return CwtConfigCompletionContext(
            globalContext = globalContext,
            configGroup = configGroup,
            keyword = keyword,
            expressionElement = expressionElement,
            containerElement = containerElement,
            keyToMatch = keyToMatch,
            optionContainerIdToMatch = optionContainerIdToMatch,
            schema = schema,
            contextConfigs = contextConfigs,
            isOptionKey = isOptionKey,
            isOptionValue = isOptionValue,
            isOptionBlockValue = isOptionBlockValue,
            inOption = inOption,
            isPropertyKey = isPropertyKey,
            isPropertyValue = isPropertyValue,
            isBlockValue = isBlockValue,
            isKey = isKey,
            isKeyOnly = isKeyOnly,
            isValueOnly = isValueOnly,
        )
    }

    private fun getExpressionElement(element: PsiElement): CwtExpressionElement? {
        if (element is CwtOptionKey || (element is CwtString && (element.isOptionValue() || element.isOptionBlockValue()))) {
            val parentElementType = element.parent.elementType ?: return null
            if (parentElementType != CwtElementTypes.OPTION_COMMENT && parentElementType != CwtElementTypes.OPTION) return null
            val memberElement = element.parentOfType<CwtOptionComment>()?.siblings(withSelf = false)?.findIsInstance<CwtMember>() ?: return null
            return when (memberElement) {
                is CwtProperty -> memberElement.propertyKey
                is CwtValue -> memberElement
                else -> null
            }
        }
        return element.castOrNull()
    }

    private fun getContainerElement(expressionElement: PsiElement): PsiElement? {
        val result = when {
            expressionElement is CwtPropertyKey -> expressionElement.parent?.parent
            expressionElement is CwtString -> expressionElement.parent
            else -> null
        }
        if (result !is CwtProperty && result !is CwtBlockElement) return null
        return result
    }

    private fun getKeyToMatch(element: PsiElement): String? {
        if (element !is CwtString) return null
        val keyElement = element.parent?.firstChild?.takeIf { it is CwtOptionKey || it is CwtPropertyKey } ?: return null
        return keyElement.text.unquote()
    }

    private fun getOptionContainerIdToMatch(expressionElement: PsiElement): String? {
        return when {
            expressionElement is CwtPropertyKey -> "#" + expressionElement.value
            expressionElement is CwtValue -> expressionElement.value
            else -> null
        }
    }
}

// endregion
