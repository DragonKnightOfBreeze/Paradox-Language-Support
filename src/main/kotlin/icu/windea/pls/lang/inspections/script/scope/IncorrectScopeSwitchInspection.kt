package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configGroup.definitionTypesSkipCheckSystemScope
import icu.windea.pls.core.pass
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxParameterizedScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.expression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.findParentDefinition
import javax.swing.JComponent

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
    private var checkForSystemScopes = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptProperty) visitScriptProperty(element)
            }

            private fun visitScriptProperty(element: ParadoxScriptProperty) {
                val configs = ParadoxExpressionManager.getConfigs(element)
                val config = configs.firstOrNull()
                if (config == null) return
                val definitionInfo by lazy { element.findParentDefinition()?.definitionInfo }
                if (config is CwtPropertyConfig && config.configExpression.type == CwtDataTypes.ScopeField) {
                    val resultScopeContext = ParadoxScopeManager.getSwitchedScopeContext(element)
                    if (resultScopeContext == null) return
                    val links = resultScopeContext.links
                    if (links.isEmpty()) return
                    val propertyKey = element.propertyKey
                    for ((scopeNode, scopeContext) in links) {
                        val rangeInExpression = scopeNode.rangeInExpression
                        when (scopeNode) {
                            is ParadoxScopeNode -> {
                                val parentScopeContext = scopeContext.prev ?: continue
                                val inputScopes = scopeNode.config.inputScopes
                                val configGroup = config.configGroup
                                if (!ParadoxScopeManager.matchesScope(parentScopeContext, inputScopes, configGroup)) {
                                    val description = PlsBundle.message(
                                        "inspection.script.incorrectScopeSwitch.desc.1",
                                        scopeNode.text, inputScopes.joinToString(), parentScopeContext.scope.id
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            //TODO 1.3.0+ dynamic value
                            is ParadoxDynamicScopeLinkNode -> {

                            }
                            //NOTE may depend on usages
                            //skip if checkForSystemScopes is false
                            //skip if root parent scope context is not from event, scripted_trigger or scripted_effect
                            is ParadoxSystemScopeNode -> {
                                if (!checkForSystemScopes) continue
                                if (scopeContext.scope.id == ParadoxScopeManager.unknownScopeId) {
                                    val definitionType = definitionInfo?.type ?: continue
                                    if (config.configGroup.definitionTypesSkipCheckSystemScope.contains(definitionType)) continue
                                    val description = PlsBundle.message(
                                        "inspection.script.incorrectScopeSwitch.desc.3",
                                        scopeNode.text
                                    )
                                    holder.registerProblem(propertyKey, rangeInExpression, description)
                                }
                            }
                            is ParadoxParameterizedScopeLinkNode -> pass()
                            //error
                            is ParadoxErrorScopeLinkNode -> break
                        }
                    }
                }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectScopeSwitch.option.checkForSystemScope"))
                    .bindSelected(::checkForSystemScopes)
                    .actionListener { _, component -> checkForSystemScopes = component.isSelected }
            }
        }
    }
}
