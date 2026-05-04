package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingDefineVariablesFix
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 对定值变量的不正确的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名同命名空间的定值变量，那么就说存在对此定值变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 基于其使用的覆盖方式进行检查。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class IncorrectOverrideForDefineVariableInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()

                val overrideResult = ParadoxOverrideService.getOverrideResultForDefineVariable(element, file)
                if (overrideResult == null) return
                if (ParadoxOverrideService.isOverrideCorrect(overrideResult)) return

                val locationElement = element.propertyKey
                val (key, target, results, overrideStrategy) = overrideResult
                val description = PlsBundle.message("inspection.incorrectOverrideForDefineVariable.desc", key, overrideStrategy)
                val fix = NavigateToOverridingDefineVariablesFix(key, target, results)
                holder.registerProblem(locationElement, description, fix)
            }
        }
    }
}
