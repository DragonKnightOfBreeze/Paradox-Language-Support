package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.ParadoxModifierCategoryService
import icu.windea.pls.lang.scope.ParadoxScopeService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.ParadoxScriptVisitor

class IncorrectScopeInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }

            override fun visitValue(element: ParadoxScriptValue) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptMember, holder: ProblemsHolder) {
        val configs = ParadoxConfigManager.getConfigs(element)
        val config = configs.firstOrNull() ?: return
        if (!ParadoxScopeManager.isScopeContextSupported(element)) return
        val parentMember = ParadoxScopeManager.findParentMember(element, withSelf = false) ?: return
        val parentScopeContext = ParadoxScopeManager.getScopeContext(parentMember) ?: return
        val supportedScopes = getSupportedScopes(element, config) ?: return
        val configGroup = config.configGroup
        if (!ParadoxScopeManager.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
            val supportedScopesText = supportedScopes.joinToString()
            val currentScopeText = parentScopeContext.scope.id
            if (element is ParadoxScriptProperty) {
                val propertyKey = element.propertyKey
                val text = propertyKey.presentableText
                val description = ChronicleInspectionBundle.message("script.incorrectScope.desc.1", text, supportedScopesText, currentScopeText)
                holder.registerProblem(propertyKey, description)
            } else if (element is ParadoxScriptString && config.configExpression.type == CwtDataTypes.AliasKeysField) {
                val text = element.presentableText
                val description = ChronicleInspectionBundle.message("script.incorrectScope.desc.2", text, supportedScopesText, currentScopeText)
                holder.registerProblem(element, description)
            }
        }
    }

    private fun getSupportedScopes(element: ParadoxScriptMember, config: CwtMemberConfig<*>): Set<String>? {
        if (config.configExpression.type == CwtDataTypes.AliasKeysField) {
            val configGroup = config.configGroup
            val aliasName = config.configExpression.metadata.value ?: return null
            val aliasSubName = element.name ?: return null
            val aliasConfig = configGroup.aliasGroups.get(aliasName)?.get(aliasSubName)?.singleOrNull() ?: return null
            val supportedScopes = aliasConfig.supportedScopes
            return supportedScopes
        }
        if (config.configExpression.type == CwtDataTypes.Modifier) {
            val expressionElement = getExpressionElement(element) ?: return null
            if (expressionElement !is ParadoxScriptStringExpressionElement) return null
            ProgressManager.checkCanceled()
            val resolved = expressionElement.reference?.resolve() ?: return null
            if (resolved !is ParadoxModifierLightElement) return null
            val modifierCategories = ParadoxModifierCategoryService.getModifierCategories(resolved)
            return modifierCategories?.let { ParadoxScopeManager.getSupportedScopes(it) }
        }
        if (config.configExpression.type == CwtDataTypes.Definition) {
            val expressionElement = getExpressionElement(element) ?: return null
            ProgressManager.checkCanceled()
            val resolved = expressionElement.reference?.resolve()
            if (resolved !is ParadoxDefinitionElement) return null
            val definitionInfo = resolved.definitionInfo ?: return null
            val supportedScopes = ParadoxScopeService.getSupportedScopes(resolved, definitionInfo)
            return supportedScopes
        }
        val supportedScopes = config.optionMetadata.supportedScopes
        return supportedScopes
    }

    private fun getExpressionElement(element: ParadoxScriptMember): ParadoxScriptExpressionElement? {
        return when {
            element is ParadoxScriptProperty -> element.propertyKey
            element is ParadoxScriptValue -> element
            else -> null
        }
    }
}

