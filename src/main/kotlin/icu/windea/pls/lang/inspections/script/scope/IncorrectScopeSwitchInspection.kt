package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.pass
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeId
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.JComponent

class IncorrectScopeSwitchInspection : ScopeInspectionBase() {
    private var checkForSystemScopes = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptProperty) visitProperty(element)
            }

            private fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val configs = ParadoxConfigManager.getConfigs(element)
                val config = configs.firstOrNull() ?: return
                if (config.configExpression.type != CwtDataTypes.ScopeField) return
                if (config !is CwtPropertyConfig) return
                checkExpression(holder, element, config)
                return
            }
        }
    }

    private fun checkExpression(holder: ProblemsHolder, element: ParadoxScriptProperty, config: CwtPropertyConfig) {
        val resultScopeContext = ParadoxScopeManager.getScopeContext(element) ?: return
        val links = resultScopeContext.links
        if (links.isEmpty()) return
        val definitionType by lazy { findParentDefinitionType(element) }
        val propertyKey = element.propertyKey
        for ((node, scopeContext) in links) {
            val rangeInExpression = node.rangeInExpression
            when (node) {
                // NOTE may depend on usages
                // skip if checkForSystemScopes is false
                // skip if root parent scope context is not from event, scripted_trigger or scripted_effect
                is ParadoxSystemScopeNode -> {
                    if (!checkForSystemScopes) continue
                    if (scopeContext.scope.id == ParadoxScopeId.unknownScopeId) {
                        val definitionType = definitionType ?: continue
                        if (definitionType in config.configGroup.definitionTypesModel.skipCheckSystemScope) continue
                        val description = PlsBundle.message("inspection.script.incorrectScopeSwitch.desc.3", node.text)
                        holder.registerProblem(propertyKey, rangeInExpression, description)
                    }
                }
                is ParadoxScopeNode -> {
                    val parentScopeContext = scopeContext.prev ?: continue
                    val inputScopes = node.config.inputScopes
                    val configGroup = config.configGroup
                    if (ParadoxScopeManager.matchesScope(parentScopeContext, inputScopes, configGroup)) continue
                    val description = PlsBundle.message("inspection.script.incorrectScopeSwitch.desc.1", node.text, inputScopes.joinToString(), parentScopeContext.scope.id)
                    holder.registerProblem(propertyKey, rangeInExpression, description)
                }
                // TODO 1.3.0+ dynamic value
                is ParadoxDynamicScopeLinkNode -> {

                }
                is ParadoxParameterizedScopeLinkNode -> pass()
                is ParadoxErrorScopeLinkNode -> break
            }
        }
    }

    private fun findParentDefinitionType(element: ParadoxScriptProperty): String? {
        val fromElement = selectScope { element.parentDefinitionOrInjection() } ?: return null
        ParadoxDefinitionManager.getType(fromElement)?.let { return it }
        if (fromElement is ParadoxScriptProperty) {
            ParadoxDefinitionInjectionManager.getType(fromElement)?.let { return it }
        }
        return null
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // checkForSystemScopes
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectScopeSwitch.option.checkForSystemScope"))
                    .bindSelected(::checkForSystemScopes.toAtomicProperty())
            }
        }
    }
}
