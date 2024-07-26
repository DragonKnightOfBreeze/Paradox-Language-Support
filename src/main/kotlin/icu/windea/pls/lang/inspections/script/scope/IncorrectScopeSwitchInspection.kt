package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
    private var checkForSystemLinks = false
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptProperty) visitScriptProperty(element)
            }
            
            private fun visitScriptProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val configs = ParadoxExpressionHandler.getConfigs(element)
                val config = configs.firstOrNull()
                if(config == null) return
                val definitionInfo by lazy { element.findParentDefinition()?.definitionInfo }
                if(config is CwtPropertyConfig && config.expression.type == CwtDataTypes.ScopeField) {
                    val resultScopeContext = ParadoxScopeHandler.getSwitchedScopeContext(element)
                    if(resultScopeContext == null) return
                    val scopeFieldInfo = resultScopeContext.scopeFieldInfo
                    if(scopeFieldInfo.isNullOrEmpty()) return
                    val propertyKey = element.propertyKey
                    for((scopeNode, scopeContext) in scopeFieldInfo) {
                        val rangeInExpression = scopeNode.rangeInExpression
                        when(scopeNode) {
                            is ParadoxScopeLinkNode -> {
                                val parentScopeContext = scopeContext.prev ?: continue
                                val inputScopes = scopeNode.config.inputScopes
                                val configGroup = config.configGroup
                                if(!ParadoxScopeHandler.matchesScope(parentScopeContext, inputScopes, configGroup)) {
                                    val description = PlsBundle.message(
                                        "inspection.script.incorrectScopeSwitch.desc.1",
                                        scopeNode.text, inputScopes.joinToString(), parentScopeContext.scope.id
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            //TODO 1.3.0+ dynamic value (expression)
                            is ParadoxScopeLinkFromDataNode -> {
                                
                            }
                            //NOTE may depend on usages
                            //check when root parent scope context is not from event, scripted_trigger or scripted_effect
                            is ParadoxSystemLinkNode -> {
                                if(!checkForSystemLinks) continue
                                if(scopeContext.scope.id == ParadoxScopeHandler.unknownScopeId) {
                                    val definitionType = definitionInfo?.type ?: continue
                                    if(config.configGroup.definitionTypesSkipCheckSystemLink.contains(definitionType)) continue
                                    val description = PlsBundle.message(
                                        "inspection.script.incorrectScopeSwitch.desc.3",
                                        scopeNode.text
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            is ParadoxParameterizedScopeFieldNode -> pass()
                            //error
                            is ParadoxErrorScopeFieldNode -> break
                        }
                    }
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectScopeSwitch.option.checkForSystemLinks"))
                    .bindSelected(::checkForSystemLinks)
                    .actionListener { _, component -> checkForSystemLinks = component.isSelected }
            }
        }
    }
}
