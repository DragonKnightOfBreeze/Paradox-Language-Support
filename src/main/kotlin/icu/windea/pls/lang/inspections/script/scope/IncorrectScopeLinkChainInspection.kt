package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

class IncorrectScopeLinkChainInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                check(element, configGroup, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        val dataType = config.configExpression.type
        val value = element.value
        val complexExpression = when {
            dataType in CwtDataTypeSets.DynamicValue -> ParadoxDynamicValueExpression.resolve(value, null, configGroup, config)
            dataType in CwtDataTypeSets.ScopeField -> ParadoxScopeFieldExpression.resolve(value, null, configGroup)
            dataType in CwtDataTypeSets.ValueField -> ParadoxValueFieldExpression.resolve(value, null, configGroup)
            dataType in CwtDataTypeSets.VariableField -> ParadoxVariableFieldExpression.resolve(value, null, configGroup)
            else -> null
        }
        if (complexExpression == null) return
        check(element, complexExpression, holder)
    }

    private fun check(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression, holder: ProblemsHolder) {
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression) checkIn(element, node, holder)
                return super.visit(node)
            }
        })
    }

    private fun checkIn(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression, holder: ProblemsHolder) {
        val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxScopeNode>()
        val max = ParadoxScopeManager.maxScopeLinkSize
        val actual = scopeNodes.size
        if (actual <= max) return
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
        val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
        val range = TextRange.create(startOffset, endOffset)
        val description = ChronicleBundle.message("inspection.script.incorrectScopeLinkChain.desc.1", max, actual)
        holder.registerProblem(element, range, description)
    }
}
