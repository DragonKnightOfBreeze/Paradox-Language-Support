package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
    private var checkForSystemLink = false
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationCommandScope) visitLocalisationCommandScope(element)
            }
            
            private fun visitLocalisationCommandScope(element: ParadoxLocalisationCommandScope) {
                ProgressManager.checkCanceled()
                val resolved = element.reference.resolve() ?: return
                when {
                    //system link or localisation scope
                    resolved is CwtProperty -> {
                        val config = resolved.getUserData(PlsKeys.cwtConfig)
                        when(config) {
                            is CwtLocalisationLinkConfig -> {
                                val scopeContext = ParadoxScopeHandler.getScopeContext(element) ?: return
                                val supportedScopes = config.inputScopes
                                val configGroup = config.configGroup
                                if(!ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)) {
                                    val description = PlsBundle.message(
                                        "inspection.localisation.incorrectScopeSwitch.description.1",
                                        element.name, supportedScopes.joinToString(), scopeContext.scope.id
                                    )
                                    holder.registerProblem(element, description)
                                }
                            }
                            //NOTE depends on usages, cannot check now
                            //is CwtSystemLinkConfig -> {
                            // if(!checkForSystemLink) return
                            //	val scopeContext = ParadoxScopeHandler.getScopeContext(element, file) ?: return
                            //	val resolvedScope = ParadoxScopeHandler.resolveScopeBySystemLink(config, scopeContext)
                            //	if(resolvedScope == null) {
                            //		val location = element
                            //		val description = PlsBundle.message("inspection.localisation.incorrectScopeSwitch.description.3",
                            //			element.name)
                            //		holder.registerProblem(location, description)
                            //	}
                            //}
                        }
                    }
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.localisation.incorrectScopeSwitch.option.checkForSystemLink"))
                    .bindSelected(::checkForSystemLink)
                    .actionListener { _, component -> checkForSystemLink = component.isSelected }
            }
        }
    }
}
