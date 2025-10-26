package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingScriptedVariablesFix
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 不正确的对（全局）封装变量的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名的封装变量，那么就说存在对此封装变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于非参数化的全局封装变量。
 * - 基于其使用的覆盖方式进行检查。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class IncorrectOverrideForScriptedVariableInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR
        if (!ParadoxScriptedVariableManager.isGlobalFilePath(fileInfo.path)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptScriptedVariable) visitScriptedVariable(element)
            }

            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val overrideResult = ParadoxOverrideService.getOverrideResultForGlobalScriptedVariable(element, file)
                if (overrideResult == null) return
                if (ParadoxOverrideService.isOverrideCorrect(overrideResult)) return

                val locationElement = element.scriptedVariableName
                val (key, target, results, overrideStrategy) = overrideResult
                val message = PlsBundle.message("inspection.incorrectOverrideForScriptedVariable.desc", key, overrideStrategy)
                val fix = NavigateToOverridingScriptedVariablesFix(key, target, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}
