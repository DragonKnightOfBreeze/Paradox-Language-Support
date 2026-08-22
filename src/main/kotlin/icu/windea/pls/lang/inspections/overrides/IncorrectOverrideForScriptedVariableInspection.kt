package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fixes.navigation.NavigateToOverridingScriptedVariablesFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否存在不正确的对（全局）封装变量（global scripted variable）的重载。
 *
 * 说明：
 * - 如果当前上下文中存在同名的封装变量，那么就说存在对此封装变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于非参数化的全局封装变量。
 * - 基于其使用的覆盖策略进行检查。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class IncorrectOverrideForScriptedVariableInspection : OverrideRelatedInspectionBase() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return super.isAvailableForFile(file) && ParadoxScriptedVariableManager.isGlobalScriptedVariablesFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptScriptedVariable, holder: ProblemsHolder) {
        val overrideResult = ParadoxOverrideService.getOverrideResultForGlobalScriptedVariable(element, holder.file)
        if (overrideResult == null) return
        if (ParadoxOverrideService.isOverrideCorrect(overrideResult)) return

        val locationElement = element.scriptedVariableName
        val (key, target, results, overrideStrategy) = overrideResult
        val description = ChronicleInspectionBundle.message("overrides.incorrectOverrideForScriptedVariable.desc", key, overrideStrategy)
        val fix = NavigateToOverridingScriptedVariablesFix(key, target, results)
        holder.registerProblem(locationElement, description, fix)
    }
}
