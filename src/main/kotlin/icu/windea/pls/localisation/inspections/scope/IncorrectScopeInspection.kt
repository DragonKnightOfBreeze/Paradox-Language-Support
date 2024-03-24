package icu.windea.pls.localisation.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxLocalisationCommandField) visitLocalisationCommandField(element)
            }
            
            private fun visitLocalisationCommandField(element: ParadoxLocalisationCommandField) {
                ProgressManager.checkCanceled()
                val resolved = element.reference?.resolve() ?: return
                when {
                    //predefined localisation command
                    resolved is CwtProperty -> {
                        val config = resolved.getUserData(PlsKeys.cwtConfig)
                        when(config) {
                            is CwtLocalisationCommandConfig -> {
                                val scopeContext = ParadoxScopeHandler.getScopeContext(element) ?: return
                                val supportedScopes = config.supportedScopes
                                val configGroup = config.info.configGroup
                                if(!ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)) {
                                    val location = element
                                    val description = PlsBundle.message(
                                        "inspection.localisation.incorrectScope.description.1",
                                        element.name, supportedScopes.joinToString(), scopeContext.scope.id
                                    )
                                    holder.registerProblem(location, description)
                                }
                            }
                        }
                    }
                    //NOTE scripted loc - any scope
                    resolved is ParadoxScriptProperty -> {
                        return
                    }
                    //variable
                    resolved is ParadoxDynamicValueElement -> {
                        val scopeContext = ParadoxScopeHandler.getScopeContext(element) ?: return
                        val supportedScopeContext = ParadoxScopeHandler.getScopeContext(resolved)
                        val supportedScope = supportedScopeContext.scope.id
                        val configGroup = getConfigGroup(resolved.project, resolved.gameType)
                        if(!ParadoxScopeHandler.matchesScope(scopeContext, supportedScope, configGroup)) {
                            val description = PlsBundle.message(
                                "inspection.localisation.incorrectScope.description.2",
                                element.name, supportedScope, scopeContext.scope.id
                            )
                            holder.registerProblem(element, description)
                        }
                    }
                }
            }
        }
    }
}