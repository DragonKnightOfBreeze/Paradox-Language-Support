@file:Suppress("KotlinConstantConditions")

package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.CwtType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.Icon

fun CompletionResultSet.addElement(lookupElement: LookupElement?, context: ProcessingContext) {
    if (lookupElement == null) return
    getFinalElement(lookupElement, context)?.let { addElement(it) }
    lookupElement.extraLookupElements?.forEach { extraLookupElement ->
        getFinalElement(extraLookupElement, context)?.let { addElement(it) }
    }
}

private fun getFinalElement(lookupElement: LookupElement, context: ProcessingContext): LookupElement? {
    val completionIds = context.completionIds
    if (completionIds?.let { ids -> lookupElement.completionId?.let { id -> ids.add(id) } } == false) return null
    val priority = lookupElement.priority
    if (priority != null) return PrioritizedLookupElement.withPriority(lookupElement, priority)
    return lookupElement
}

fun <T : LookupElement> T.withPriority(priority: Double?): T {
    val scopeMatched = this.scopeMatched
    if (priority == null && scopeMatched) return this
    var finalPriority = priority ?: 0.0
    if (!scopeMatched) finalPriority += PlsCompletionPriorities.scopeMismatchOffset
    this.priority = finalPriority
    return this
}

fun <T : LookupElement> T.withCompletionId(completionId: String = lookupString): T {
    this.completionId = completionId
    return this
}

fun <T : LookupElement> T.withPatchableIcon(icon: Icon?): T {
    this.patchableIcon = icon
    return this
}

fun <T : LookupElement> T.withPatchableTailText(tailText: String?): T {
    this.patchableTailText = tailText
    return this
}

fun <T : LookupElement> T.withForceInsertCurlyBraces(forceInsertCurlyBraces: Boolean): T {
    this.forceInsertCurlyBraces = forceInsertCurlyBraces
    return this
}

fun LookupElementBuilder.withScopeMatched(scopeMatched: Boolean): LookupElementBuilder {
    this.scopeMatched = scopeMatched
    if (scopeMatched) return this
    return withItemTextForeground(JBColor.GRAY)
}

fun LookupElementBuilder.withScriptedVariableLocalizedNamesIfNecessary(element: ParadoxScriptScriptedVariable): LookupElementBuilder {
    if (PlsSettings.getInstance().state.completion.completeByLocalizedName) {
        ProgressManager.checkCanceled()
        localizedNames = ParadoxScriptedVariableManager.getLocalizedNames(element)
    }
    return this
}

fun LookupElementBuilder.withDefinitionLocalizedNamesIfNecessary(element: ParadoxDefinitionElement): LookupElementBuilder {
    if (PlsSettings.getInstance().state.completion.completeByLocalizedName) {
        ProgressManager.checkCanceled()
        localizedNames = ParadoxDefinitionManager.getLocalizedNames(element)
    }
    return this
}

fun LookupElementBuilder.withModifierLocalizedNamesIfNecessary(modifierName: String, element: PsiElement): LookupElementBuilder {
    if (PlsSettings.getInstance().state.completion.completeByLocalizedName) {
        ProgressManager.checkCanceled()
        localizedNames = ParadoxModifierManager.getModifierLocalizedNames(modifierName, element, element.project)
    }
    return this
}

fun LookupElementBuilder.forScriptExpression(context: ProcessingContext): LookupElementBuilder? {
    // check whether scope is matched again here
    if ((!scopeMatched || !context.scopeMatched) && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return null

    val config = context.config
    val completeWithValue = PlsSettings.getInstance().state.completion.completeWithValue
    val targetConfig = when {
        config is CwtPropertyConfig -> config
        config is CwtAliasConfig -> config.config
        config is CwtSingleAliasConfig -> config.config
        config is CwtDirectiveConfig -> config.config
        else -> null
    }?.let { c -> CwtConfigManipulator.inlineSingleAlias(c) ?: c } // 这里需要进行必要的内联

    val contextElement = context.contextElement
    val isKeyElement = contextElement is ParadoxScriptPropertyKey
    val isStringElement = contextElement is ParadoxScriptString
    val isKey = context.isKey
    val isBlockConfig = targetConfig?.let { it.valueType == CwtType.Block } ?: false
    val constantValue = when {
        completeWithValue -> targetConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }?.value
        else -> null
    }
    val insertCurlyBraces = when {
        forceInsertCurlyBraces -> true
        completeWithValue -> isBlockConfig
        else -> false
    }

    // 排除重复项
    val withValueText = when {
        isKeyElement || (isStringElement && isKey != true) -> ""
        constantValue != null -> " = $constantValue"
        insertCurlyBraces -> " = {...}"
        else -> ""
    }
    val completionId = lookupString + withValueText
    if (context.completionIds?.add(completionId) == false) return null

    var lookupElement = this

    lookupElement = lookupElement.addLocalizedNames()
    lookupElement = lookupElement.patchIcon(config)
    lookupElement = lookupElement.patchTailText(withValueText)

    if (!isKeyElement && !isStringElement) return lookupElement // not in a key or value position
    if (isKey == null) return lookupElement // not complete full key or value

    val params = PlsInsertHandlers.Params(
        quoted = context.quoted,
        isKey = context.isKey,
        insertCurlyBraces = insertCurlyBraces,
        constantValue = constantValue,
    )

    if (isKeyElement || isStringElement && !isKey) { // key or value only
        lookupElement = lookupElement.withInsertHandler(PlsInsertHandlers.keyOrValue(params))
    } else if (isKey) { // key with value
        lookupElement = lookupElement.withInsertHandler(PlsInsertHandlers.keyWithValue(params))
    }

    val extraLookupElements = mutableListOf<LookupElement>()

    // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
    if (isKey && !isKeyElement && isBlockConfig && config != null) {
        val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildLookupElement(context, config, lookupElement)
        if (extraLookupElement != null) extraLookupElements.add(extraLookupElement)
    }

    lookupElement.extraLookupElements = extraLookupElements
    return lookupElement
}

private fun LookupElementBuilder.addLocalizedNames(): LookupElementBuilder {
    val localizedNames = localizedNames
    if (localizedNames.isNullOrEmpty()) return this
    return withLookupStrings(localizedNames)
}

private fun LookupElementBuilder.patchIcon(config: CwtConfig<*>?): LookupElementBuilder {
    val patchableIcon = patchableIcon
    if (patchableIcon == null) return this
    return withIcon(getPatchedIcon(patchableIcon, config))
}

private fun getPatchedIcon(icon: Icon?, config: CwtConfig<*>?): Icon? {
    if (icon == null) return null
    when (config) {
        is CwtValueConfig -> {
            if (config.tagType != null) return PlsIcons.Nodes.Tag
        }
        is CwtAliasConfig -> {
            val aliasConfig = config
            val type = aliasConfig.configExpression.type
            if (type !in CwtDataTypeSets.ConstantAware) return icon
            val aliasName = aliasConfig.name
            return when {
                aliasName == "modifier" -> PlsIcons.Nodes.Modifier
                aliasName == "trigger" -> PlsIcons.Nodes.Trigger
                aliasName == "effect" -> PlsIcons.Nodes.Effect
                else -> icon
            }
        }
    }
    return icon
}

private fun LookupElementBuilder.patchTailText(withValueText: String): LookupElementBuilder {
    val patchableTailText = patchableTailText
    val finalTailText = buildString {
        append(withValueText)
        if (patchableTailText != null) append(patchableTailText)
    }
    if (finalTailText.isEmpty()) return this
    return withTailText(finalTailText, true)
}
