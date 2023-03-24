package icu.windea.pls.script.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            ProgressManager.checkCanceled()
            if(element is ParadoxScriptMemberElement) visitMemberElement(element)
        }
        
        private fun visitMemberElement(element: ParadoxScriptMemberElement) {
            val configs = ParadoxConfigHandler.getConfigs(element)
            val config = configs.firstOrNull() ?: return
            if(!ParadoxScopeHandler.isScopeContextSupported(element)) return
            val parentMember = ParadoxScopeHandler.findParentMember(element) ?: return
            val parentScopeContext = ParadoxScopeHandler.getScopeContext(parentMember) ?: return
            val supportedScopes = getSupportedScopes(element, config) ?: return
            val configGroup = config.info.configGroup
            if(!ParadoxScopeHandler.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
                if(element is ParadoxScriptProperty) {
                    val propertyKey = element.propertyKey
                    val description = PlsBundle.message("inspection.script.scope.incorrectScope.description.1",
                        propertyKey.expression, supportedScopes.joinToString(), parentScopeContext.scope.id)
                    holder.registerProblem(propertyKey, description)
                } else if(element is ParadoxScriptString && config.expression.type == CwtDataType.AliasKeysField) {
                    val description = PlsBundle.message("inspection.script.scope.incorrectScope.description.2",
                        element.expression, supportedScopes.joinToString(), parentScopeContext.scope.id)
                    holder.registerProblem(element, description)
                }
            }
        }
        
        private fun getSupportedScopes(element: ParadoxScriptMemberElement, config: CwtDataConfig<*>): Set<String>? {
            if(config.expression.type == CwtDataType.AliasKeysField) {
                val configGroup = config.info.configGroup
                val aliasName = config.expression.value ?: return null
                val aliasSubName = element.name ?: return null
                val aliasConfig = configGroup.aliasGroups.get(aliasName)?.get(aliasSubName)?.singleOrNull() ?: return null
                val supportedScopes = aliasConfig.supportedScopes
                return supportedScopes
            }
            val supportedScopes = config.supportedScopes
            return supportedScopes
        }
    }
}

