package icu.windea.pls.localisation.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
    private var checkForSystemLink = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if(element is ParadoxLocalisationCommandScope) visitLocalisationCommandScope(element)
        }
        
        private fun visitLocalisationCommandScope(element: ParadoxLocalisationCommandScope) {
            val resolved = element.reference.resolve() ?: return
            when {
                //system link or localisation scope
                resolved is CwtProperty -> {
                    val config = resolved.getUserData(PlsKeys.cwtConfigKey)
                    when {
                        config is CwtLocalisationLinkConfig -> {
                            val scopeContext = ParadoxScopeHandler.getScopeContext(element) ?: return
                            val supportedScopes = config.inputScopes
                            val configGroup = config.info.configGroup
                            if(!ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)) {
                                val description = PlsBundle.message("inspection.localisation.scope.incorrectScopeSwitch.description.1",
                                    element.name, supportedScopes.joinToString(), scopeContext.thisScope)
                                holder.registerProblem(element, description)
                            }
                        }
                        //TODO depends on usages, cannot check now
                        //config is CwtSystemLinkConfig -> {
                        // if(!checkForSystemLink) return
                        //	val scopeContext = ParadoxScopeHandler.getScopeContext(element, file) ?: return
                        //	val resolvedScope = ParadoxScopeHandler.resolveScopeBySystemLink(config, scopeContext)
                        //	if(resolvedScope == null) {
                        //		val location = element
                        //		val description = PlsBundle.message("inspection.localisation.scope.incorrectScopeSwitch.description.3",
                        //			element.name)
                        //		holder.registerProblem(location, description)
                        //	}
                        //}
                    }
                }
                //TODO event target or global event target - not supported yet
                resolved is ParadoxValueSetValueElement -> {
                    return
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.localisation.scope.incorrectScopeSwitch.option.checkForSystemLink"))
                    .bindSelected(::checkForSystemLink)
                    .actionListener { _, component -> checkForSystemLink = component.isSelected }
            }
        }
    }
}
