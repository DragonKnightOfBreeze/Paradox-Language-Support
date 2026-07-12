package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.fixes.navigation.NavigateToOverridingDefineVariablesFix
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 对定值变量的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名同命名空间的定值变量，那么就说存在对此定值变量的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class OverrideForDefineVariableInspection : OverrideRelatedInspectionBase() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return super.isAvailableForFile(file) && ParadoxDefineManager.isDefinesFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()

                val overrideResult = ParadoxOverrideService.getOverrideResultForDefineVariable(element, holder.file)
                if (overrideResult == null) return

                val locationElement = element.propertyKey
                val (key, target, results) = overrideResult
                val description = ChronicleBundle.message("inspection.overrideForDefineVariable.desc", key)
                val fix = NavigateToOverridingDefineVariablesFix(key, target, results)
                holder.registerProblem(locationElement, description, fix)
            }
        }
    }
}
