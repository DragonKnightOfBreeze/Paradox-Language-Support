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
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxCompletionUtil {
    fun processDefinition(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return true
        if (definitionInfo.name.isEmpty()) return true // skip anonymous definitions
        val name = definitionInfo.name
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableTailText(context.patchableTailText)
            .withDefinitionLocalizedNamesIfNecessary(element)
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
