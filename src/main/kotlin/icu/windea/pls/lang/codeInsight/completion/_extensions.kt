@file:Suppress("KotlinConstantConditions")

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
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import javax.swing.Icon

fun CompletionResultSet.addElement(lookupElement: LookupElement?, context: CompletionContext) {
    if (lookupElement == null) return
    getFinalElement(lookupElement, context)?.let { addElement(it) }
    lookupElement.extraLookupElements?.forEach { extraLookupElement ->
        getFinalElement(extraLookupElement, context)?.let { addElement(it) }
    }
}

fun CompletionResultSet.addElements(lookupElements: Collection<LookupElement>, context: CompletionContext) {
    for (lookupElement in lookupElements) addElement(lookupElement, context)
}

private fun getFinalElement(lookupElement: LookupElement, context: CompletionContext): LookupElement? {
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
    presentableNames = ParadoxScriptedVariableManager.getPresentableNames(element)
    return this
}

fun LookupElementBuilder.withDefinitionPresentableNames(element: ParadoxDefinitionElement): LookupElementBuilder {
    if (!ChronicleSettings.getInstance().state.completion.completeByPresentableName) return this

    ProgressManager.checkCanceled()
    presentableNames = ParadoxDefinitionManager.getPresentableNames(element)
    return this
}

fun LookupElementBuilder.withModifierPresentableNames(modifierName: String, element: ParadoxScriptStringExpressionElement): LookupElementBuilder {
    if (!ChronicleSettings.getInstance().state.completion.completeByPresentableName) return this

    ProgressManager.checkCanceled()
    presentableNames = ParadoxModifierManager.getModifierPresentableNames(modifierName, element, element.project)
    return this
}

fun LookupElementBuilder.wrapForConfig(context: CwtConfigCompletionContext, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression): LookupElement? {
    return CwtCompletionLookupProvider.wrapForConfig(this, context, config, schemaExpression)
}

fun LookupElementBuilder.wrapForExpression(context: ParadoxCompletionContext): LookupElementBuilder? {
    return ParadoxCompletionLookupProvider.wrapForExpression(this, context)
}
