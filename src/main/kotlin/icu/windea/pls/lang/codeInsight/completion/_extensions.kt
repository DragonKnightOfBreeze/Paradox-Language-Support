package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.ui.JBColor
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.codeInsight.completion.CompletionContext
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import javax.swing.Icon

fun LookupElement?.addToResult(context: CompletionContext, result: CompletionResultSet): Boolean {
    addComputedElement(context, result, this)
    return true
}

fun Collection<LookupElement>.addToResult(context: CompletionContext, result: CompletionResultSet): Boolean {
    for (lookupElement in this) addComputedElement(context, result, lookupElement)
    return true
}

private fun addComputedElement(context: CompletionContext, result: CompletionResultSet, lookupElement: LookupElement?) {
    if (lookupElement == null) return
    computeElement(lookupElement, context)?.let { result.addElement(it) }
    lookupElement.extraLookupElements?.forEach { extraLookupElement ->
        computeElement(extraLookupElement, context)?.let { result.addElement(it) }
    }
}

private fun computeElement(lookupElement: LookupElement, context: CompletionContext): LookupElement? {
    val completionIds = context.completionIds
    if (lookupElement.completionId?.let { id -> completionIds.add(id) } == false) return null
    val priority = lookupElement.priority
    if (priority != null) return PrioritizedLookupElement.withPriority(lookupElement, priority)
    return lookupElement
}

fun <T : LookupElement> T.withPriority(priority: Double?): T {
    val scopeMatched = this.scopeMatched
    if (priority == null && scopeMatched) return this
    var finalPriority = priority ?: 0.0
    if (!scopeMatched) finalPriority += ParadoxCompletionPriorities.scopeMismatchOffset
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

fun LookupElementBuilder.withScriptedVariablePresentableNames(element: ParadoxScriptScriptedVariable): LookupElementBuilder {
    if (!ChronicleSettings.getInstance().state.completion.completeByPresentableName) return this
    ProgressManager.checkCanceled()
    // TODO 3.0.1+ [performance] may be relatively slow, consider optimize performance...
    val presentableNames = ParadoxScriptedVariableManager.getPresentableNames(element)
    if (presentableNames.isEmpty()) return this
    return withLookupStrings(presentableNames)
}

fun LookupElementBuilder.withDefinitionPresentableNames(element: ParadoxDefinitionElement): LookupElementBuilder {
    if (!ChronicleSettings.getInstance().state.completion.completeByPresentableName) return this
    ProgressManager.checkCanceled()
    // TODO 3.0.1+ [performance] may be relatively slow, consider optimize performance...
    val presentableNames = ParadoxDefinitionManager.getPresentableNames(element)
    if (presentableNames.isEmpty()) return this
    return withLookupStrings(presentableNames)
}

fun LookupElementBuilder.withModifierPresentableNames(modifierName: String, context: ParadoxCompletionContext): LookupElementBuilder {
    if (!ChronicleSettings.getInstance().state.completion.completeByPresentableName) return this
    ProgressManager.checkCanceled()
    // TODO 3.0.1+ [performance] may be relatively slow, consider optimize performance...
    val presentableNames = ParadoxModifierManager.getModifierPresentableNames(modifierName, context.contextElement, context.project)
    if (presentableNames.isEmpty()) return this
    return withLookupStrings(presentableNames)
}

fun LookupElementBuilder.wrapForConfig(context: CwtConfigCompletionContext, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression): LookupElement? {
    return CwtCompletionLookupProvider.wrapForConfig(this, context, config, schemaExpression)
}

fun LookupElementBuilder.wrapForExpression(context: ParadoxCompletionContext): LookupElementBuilder? {
    return ParadoxCompletionLookupProvider.wrapForExpression(this, context)
}
