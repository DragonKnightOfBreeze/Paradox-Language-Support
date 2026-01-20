package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingDefinitionsFix
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 对定义的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名同类型的定义，那么就说存在对此定义的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于非参数化的、非匿名的、作为脚本属性的定义。
 */
class OverrideForDefinitionInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()

                val overrideResult = ParadoxOverrideService.getOverrideResultForDefinition(element, file)
                if (overrideResult == null) return

                val locationElement = element.propertyKey
                val (key, target, results) = overrideResult
                val description = PlsBundle.message("inspection.overrideForDefinition.desc", key)
                val fix = NavigateToOverridingDefinitionsFix(key, target, results)
                holder.registerProblem(locationElement, description, fix)
            }
        }
    }
}
