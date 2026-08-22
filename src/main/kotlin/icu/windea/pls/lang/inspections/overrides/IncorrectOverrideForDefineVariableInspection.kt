package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fixes.navigation.NavigateToOverridingDefineVariablesFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否存在不正确的对定值变量（define variable）的重载。
 *
 * 说明：
 * - 如果当前上下文中存在同名同命名空间的定值变量，那么就说存在对此定值变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 基于其使用的覆盖策略进行检查。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class IncorrectOverrideForDefineVariableInspection : OverrideRelatedInspectionBase() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return super.isAvailableForFile(file) && ParadoxDefineManager.isDefinesFile(file)
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
        val overrideResult = ParadoxOverrideService.getOverrideResultForDefineVariable(element, holder.file)
        if (overrideResult == null) return
        if (ParadoxOverrideService.isOverrideCorrect(overrideResult)) return

        val locationElement = element.propertyKey
        val (key, target, results, overrideStrategy) = overrideResult
        val description = ChronicleInspectionBundle.message("overrides.incorrectOverrideForDefineVariable.desc", key, overrideStrategy)
        val fix = NavigateToOverridingDefineVariablesFix(key, target, results)
        holder.registerProblem(locationElement, description, fix)
    }
}
