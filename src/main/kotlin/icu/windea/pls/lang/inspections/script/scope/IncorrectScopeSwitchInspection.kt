package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.pass
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

class IncorrectScopeSwitchInspection : ScopeInspectionBase() {
    private var checkForSystemScopes = false

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("checkForSystemScopes", ChronicleInspectionBundle.message("inspection.script.incorrectScopeSwitch.option.checkForSystemScope"))
        )
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        val configs = ParadoxConfigManager.getConfigs(element)
        val config = configs.firstOrNull() ?: return
        if (config.configExpression.type != CwtDataTypes.ScopeField) return
        if (config !is CwtPropertyConfig) return
        check(element, config, holder)
    }

    private fun check(element: ParadoxScriptProperty, config: CwtPropertyConfig, holder: ProblemsHolder) {
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
                    if (scopeContext.scope.id == ParadoxScopeConstants.unknownScope) {
                        val definitionType = definitionType ?: continue
                        if (definitionType in config.configGroup.typeModel.skipCheckSystemScope) continue
                        val description = ChronicleInspectionBundle.message("inspection.script.incorrectScopeSwitch.desc.3", node.text)
                        holder.registerProblem(propertyKey, rangeInExpression, description)
                    }
                }
                is ParadoxStaticScopeNode -> {
                    val parentScopeContext = scopeContext.prev ?: continue
                    val inputScopes = node.config.inputScopes
                    val configGroup = config.configGroup
                    if (ParadoxScopeManager.matchesScope(parentScopeContext, inputScopes, configGroup)) continue
                    val description = ChronicleInspectionBundle.message("inspection.script.incorrectScopeSwitch.desc.1", node.text, inputScopes.joinToString(), parentScopeContext.scope.id)
                    holder.registerProblem(propertyKey, rangeInExpression, description)
                }
                // TODO 1.3.0+ dynamic value
                is ParadoxDynamicScopeNode -> {

                }
                is ParadoxParameterizedScopeNode -> pass()
                is ParadoxErrorScopeNode -> break
            }
        }
    }

    private fun findParentDefinitionType(element: ParadoxScriptProperty): String? {
        val fromElement = selectScope { element.parentDefinitionCandidate() } ?: return null
        ParadoxDefinitionManager.getType(fromElement)?.let { return it }
        if (fromElement is ParadoxScriptProperty) {
            ParadoxDefinitionInjectionManager.getType(fromElement)?.let { return it }
        }
        return null
    }
}
