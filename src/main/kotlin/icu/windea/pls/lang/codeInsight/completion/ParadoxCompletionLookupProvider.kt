package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.Icon

object ParadoxCompletionLookupProvider {
    // TODO 3.0.1 重构……避免某些 manager 过大……

    fun wrapForExpression(lookupElement: LookupElementBuilder, context: ParadoxCompletionContext): LookupElementBuilder? {
        // check whether scope is matched again here
        if ((!lookupElement.scopeMatched || !context.scopeMatched) && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return null

        val config = context.config
        val completeWithValue = ChronicleSettings.getInstance().state.completion.completeWithValue
        val targetConfig = when {
            config is CwtPropertyConfig -> config
            config is CwtAliasConfig -> config.config
            config is CwtSingleAliasConfig -> config.config
            config is CwtMacroConfig -> config.config
            else -> null
        }?.let { c -> CwtConfigManipulationService.inlineForConfig(c) } // 这里需要进行必要的内联

        val contextElement = context.contextElement
        val isKeyElement = contextElement is ParadoxScriptPropertyKey
        val isStringElement = contextElement is ParadoxScriptString
        val isBlockConfig = targetConfig?.let { it.valueType == CwtExpressionType.Block } ?: false

        val lookupString = when {
            context.leftQuoted -> lookupElement.lookupString // already quoted
            else -> lookupElement.lookupString.quoteIfNeeded() // #369 should be quoted if is blank or contains blank
        }
        val constantValue = when {
            completeWithValue -> targetConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }?.expressionString
            else -> null
        }
        val insertCurlyBraces = when {
            lookupElement.forceInsertCurlyBraces -> true
            completeWithValue -> isBlockConfig
            else -> false
        }
        val withValueText = when {
            isKeyElement || (isStringElement && context.isKey != true) -> ""
            constantValue != null -> " = $constantValue"
            insertCurlyBraces -> " = {...}"
            else -> ""
        }

        // 排除重复项
        val completionId = lookupString + withValueText
        if (!context.completionIds.add(completionId)) return null

        var result = lookupElement

        result = result.withBaseLookupString(lookupString) // #369
        result = result.patchIcon(config)
        result = result.patchTailText(withValueText)
        result = result.addPresentableNames()

        if (!isKeyElement && !isStringElement) return result // not in a key or value position
        if (context.isKey == null) return result // not complete full key or value

        val params = ChronicleInsertHandlers.Params(
            quoted = context.leftQuoted,
            isKey = context.isKey,
            insertCurlyBraces = insertCurlyBraces,
            constantValue = constantValue,
        )

        if (isKeyElement || !context.isKey) { // key or value only
            result = result.withInsertHandler(ChronicleInsertHandlers.keyOrValue(params))
        } else { // key with value
            result = result.withInsertHandler(ChronicleInsertHandlers.keyWithValue(params))
        }

        val extraLookupElements = mutableListOf<LookupElement>()

        // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
        if (context.isKey && !isKeyElement && isBlockConfig && config != null) {
            val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildLookupElement(context, config, result)
            if (extraLookupElement != null) extraLookupElements.add(extraLookupElement)
        }

        result.extraLookupElements = extraLookupElements
        return result
    }

    private fun LookupElementBuilder.patchIcon(config: CwtConfig<*>?): LookupElementBuilder {
        val patchableIcon = patchableIcon
        if (patchableIcon == null) return this
        val patchedIcon = getPatchedIcon(patchableIcon, config)
        return withIcon(patchedIcon)
    }

    private fun getPatchedIcon(icon: Icon?, config: CwtConfig<*>?): Icon? {
        if (icon == null) return null
        when (config) {
            is CwtValueConfig -> {
                if (config.tagType != null) return ChronicleIcons.Nodes.Tag
            }
            is CwtAliasConfig -> {
                val aliasConfig = config
                val type = aliasConfig.configExpression.type
                if (type !in CwtDataTypeSets.ConstantAware) return icon
                val aliasName = aliasConfig.name
                return when {
                    aliasName == "modifier" -> ChronicleIcons.Nodes.Modifier
                    aliasName == "trigger" -> ChronicleIcons.Nodes.Trigger
                    aliasName == "effect" -> ChronicleIcons.Nodes.Effect
                    else -> icon
                }
            }
        }
        return icon
    }

    private fun LookupElementBuilder.patchTailText(withValueText: String): LookupElementBuilder {
        val patchableTailText = patchableTailText
        val patchedTailText = getPatchedTailText(withValueText, patchableTailText)
        if (patchedTailText.isEmpty()) return this
        return withTailText(patchedTailText, true)
    }

    private fun getPatchedTailText(withValueText: String, patchableTailText: String?): String = buildString {
        append(withValueText)
        if (patchableTailText != null) append(patchableTailText)
    }

    private fun LookupElementBuilder.addPresentableNames(): LookupElementBuilder {
        val presentableNames = presentableNames
        if (presentableNames.isNullOrEmpty()) return this
        return withLookupStrings(presentableNames)
    }
}
