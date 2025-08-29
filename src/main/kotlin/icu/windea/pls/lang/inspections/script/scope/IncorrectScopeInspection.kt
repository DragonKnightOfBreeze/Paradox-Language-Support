package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.supportedScopes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.ep.modifier.ParadoxModifierSupport
import icu.windea.pls.ep.scope.ParadoxDefinitionSupportedScopesProvider
import icu.windea.pls.lang.codeInsight.expression
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

class IncorrectScopeInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptMemberElement) visitMemberElement(element)
            }

            private fun visitMemberElement(element: ParadoxScriptMemberElement) {
                val configs = ParadoxExpressionManager.getConfigs(element)
                val config = configs.firstOrNull() ?: return
                if (!ParadoxScopeManager.isScopeContextSupported(element)) return
                val parentMember = ParadoxScopeManager.findParentMember(element, withSelf = false) ?: return
                val parentScopeContext = ParadoxScopeManager.getSwitchedScopeContext(parentMember) ?: return
                val supportedScopes = getSupportedScopes(element, config) ?: return
                val configGroup = config.configGroup
                if (!ParadoxScopeManager.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
                    if (element is ParadoxScriptProperty) {
                        val propertyKey = element.propertyKey
                        val description = PlsBundle.message(
                            "inspection.script.incorrectScope.desc.1",
                            propertyKey.expression, supportedScopes.joinToString(), parentScopeContext.scope.id
                        )
                        holder.registerProblem(propertyKey, description)
                    } else if (element is ParadoxScriptString && config.configExpression.type == CwtDataTypes.AliasKeysField) {
                        val description = PlsBundle.message(
                            "inspection.script.incorrectScope.desc.2",
                            element.expression, supportedScopes.joinToString(), parentScopeContext.scope.id
                        )
                        holder.registerProblem(element, description)
                    }
                }
            }

            private fun getSupportedScopes(element: ParadoxScriptMemberElement, config: CwtMemberConfig<*>): Set<String>? {
                if (config.configExpression.type == CwtDataTypes.AliasKeysField) {
                    val configGroup = config.configGroup
                    val aliasName = config.configExpression.value ?: return null
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
                    if (resolved !is ParadoxModifierElement) return null
                    val modifierCategories = ParadoxModifierSupport.getModifierCategories(resolved)
                    return modifierCategories?.let { ParadoxScopeManager.getSupportedScopes(it) }
                }
                if (config.configExpression.type == CwtDataTypes.Definition) {
                    val expressionElement = getExpressionElement(element) ?: return null
                    ProgressManager.checkCanceled()
                    val resolved = expressionElement.reference?.resolve()
                    if (resolved !is ParadoxScriptDefinitionElement) return null
                    val definitionInfo = resolved.definitionInfo ?: return null
                    val supportedScopes = ParadoxDefinitionSupportedScopesProvider.getSupportedScopes(resolved, definitionInfo)
                    return supportedScopes
                }
                val supportedScopes = config.supportedScopes
                return supportedScopes
            }

            private fun getExpressionElement(element: ParadoxScriptMemberElement): ParadoxScriptExpressionElement? {
                return when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptValue -> element
                    else -> null
                }
            }
        }
    }
}

