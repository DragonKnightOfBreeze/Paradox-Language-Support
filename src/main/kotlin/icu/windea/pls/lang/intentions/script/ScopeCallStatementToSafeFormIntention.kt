package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 将显式作用域调用转换为安全调用形式。
 *
 * ```paradox_script
 * # before
 * exists = owner
 * owner = { ... }
 *
 * # after (CK3/VIC3/EU5)
 * owner ?= { ... }
 *
 * # after (Stellaris)
 * owner? = { ... }
 * ```
 *
 * 如果 `exists = x` 和 `x = y` 之间存在注释，注释会在转换后移到安全形式之前。
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToSafeFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToSafeForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        val gameType = ParadoxAnalysisManager.selectGameType(element) ?: ParadoxGameType.getDefault()
        return ParadoxScopeCallStatementManipulationService.convertToSafeForm(element, context.project, gameType)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        // 必须是显式调用形式
        if (!ParadoxScopeCallStatementManipulationService.isNormalForm(element)) return false
        // 语法级别验证：键必须是字符串字面量
        val secondProperty = ParadoxScopeCallStatementManipulationService.getSecondProperty(element) ?: return false
        if (!ParadoxSyntaxService.isSafeAssignOperatorAllowed(secondProperty)) return false
        // 游戏类型必须支持安全操作符
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return when {
            ParadoxSyntaxConstraint.SafeAssignOperator.testTarget(gameType) -> true
            ParadoxSyntaxConstraint.SafeCallAssignOperator.testTarget(gameType) -> true
            else -> false
        }
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
