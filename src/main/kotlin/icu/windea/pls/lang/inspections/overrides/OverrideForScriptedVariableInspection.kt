package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingScriptedVariablesFix
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 对（全局）封装变量的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名的封装变量，那么就说存在对此封装变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于非参数化的全局封装变量。
 */
class OverrideForScriptedVariableInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR
        if (!ParadoxScriptedVariableManager.isGlobalFilePath(fileInfo.path)) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxScriptVisitor() {
            override fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                ProgressManager.checkCanceled()

                val overrideResult = ParadoxOverrideService.getOverrideResultForGlobalScriptedVariable(element, file)
                if (overrideResult == null) return

                val locationElement = element.scriptedVariableName
                val (key, target, results) = overrideResult
                val description = PlsBundle.message("inspection.overrideForScriptedVariable.desc", key)
                val fix = NavigateToOverridingScriptedVariablesFix(key, target, results)
                holder.registerProblem(locationElement, description, fix)
            }
        }
    }
}
