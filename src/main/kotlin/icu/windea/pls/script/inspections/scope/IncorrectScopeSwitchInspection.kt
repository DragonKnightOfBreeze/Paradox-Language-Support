package icu.windea.pls.script.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
    private var checkForSystemLink = false
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) visitScriptProperty(element)
            }
            
            private fun visitScriptProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val configs = ParadoxConfigResolver.getConfigs(element, element is ParadoxScriptValue, true, ParadoxConfigMatcher.Options.Default)
                val config = configs.firstOrNull()
                if(config == null) return
                val definitionInfo by lazy { element.findParentDefinition()?.definitionInfo }
                if(config is CwtPropertyConfig && config.expression.type == CwtDataType.ScopeField) {
                    val resultScopeContext = ParadoxScopeHandler.getScopeContext(element)
                    if(resultScopeContext == null) return
                    val scopeFieldInfo = resultScopeContext.scopeFieldInfo
                    if(scopeFieldInfo.isNullOrEmpty()) return
                    val propertyKey = element.propertyKey
                    for((scopeNode, scopeContext) in scopeFieldInfo) {
                        val rangeInExpression = scopeNode.rangeInExpression
                        when(scopeNode) {
                            is ParadoxScopeLinkExpressionNode -> {
                                val parentScopeContext = scopeContext.parent ?: continue
                                val inputScopes = scopeNode.config.inputScopes
                                val configGroup = config.info.configGroup
                                if(!ParadoxScopeHandler.matchesScope(parentScopeContext, inputScopes, configGroup)) {
                                    val description = PlsBundle.message(
                                        "inspection.script.scope.incorrectScopeSwitch.description.1",
                                        scopeNode.text, inputScopes.joinToString(), parentScopeContext.scope.id
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            //TODO 'event_target:xxx', not supported yet
                            is ParadoxScopeLinkFromDataExpressionNode -> {
                                
                            }
                            //TODO may depends on usages
                            //check when root parent scope context is not from event, scripted_trigger or scripted_effect
                            is ParadoxSystemLinkExpressionNode -> {
                                if(!checkForSystemLink) continue
                                if(scopeContext.scope.id == ParadoxScopeHandler.unknownScopeId) {
                                    val definitionType = definitionInfo?.type ?: continue
                                    if(config.info.configGroup.definitionTypesSkipCheckSystemLink.contains(definitionType)) continue
                                    val description = PlsBundle.message(
                                        "inspection.script.scope.incorrectScopeSwitch.description.3",
                                        scopeNode.text
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            is ParadoxParameterizedScopeFieldExpressionNode -> pass()
                            //error
                            is ParadoxErrorScopeFieldExpressionNode -> break
                        }
                    }
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.scope.incorrectScopeSwitch.option.checkForSystemLink"))
                    .bindSelected(::checkForSystemLink)
                    .actionListener { _, component -> checkForSystemLink = component.isSelected }
            }
        }
    }
}
