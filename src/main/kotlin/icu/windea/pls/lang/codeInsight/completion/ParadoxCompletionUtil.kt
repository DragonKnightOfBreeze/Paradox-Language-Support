package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxCompletionUtil {
    fun processScriptedVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name?.orNull() ?: return true
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.ScriptedVariable)
            .withScriptedVariableLocalizedNamesIfNecessary(element)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefinition(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return true
        val name = element.name.orNull() ?: return true // skip anonymous definitions
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
            .withPatchableTailText(context.patchableTailText)
            .withDefinitionLocalizedNamesIfNecessary(element)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineNamespace)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineVariable)
            .forExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun getPatchableTailText(context: ParadoxCompletionContext, config: CwtConfig<*>?, withConfigExpression: Boolean = true, withFileName: Boolean = true): String {
        context.patchableTailText?.let { return it }

        return buildString {
            if (withConfigExpression) {
                val configExpression = config?.configExpression
                if (configExpression != null) {
                    append(" by ").append(configExpression)
                }
            }
            if (withFileName) {
                val fileName = config?.resolved()?.pointer?.containingFile?.name
                if (fileName != null) {
                    append(" in ").append(fileName)
                }
            }
        }
    }

    fun isNextScopeMatched(context: ParadoxCompletionContext): Boolean {
        if (!context.scopeMatched) return false
        val supportedScopes = when {
            context.config is CwtPropertyConfig -> context.config.optionData.supportedScopes
            context.config is CwtAliasConfig -> context.config.supportedScopes
            context.config is CwtLinkConfig -> context.config.inputScopes
            else -> null
        }
        return when {
            context.scopeContext == null -> true
            else -> ParadoxScopeManager.matchesScope(context.scopeContext, supportedScopes, context.configGroup)
        }
    }
}
