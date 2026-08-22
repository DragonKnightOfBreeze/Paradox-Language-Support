package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.findChildren
import icu.windea.pls.lang.fixes.navigation.NavigateToDuplicatesFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * 检查当前本地化文件中是否存在（位于同一属性列表中的）重复的属性声明。
 *
 * 提供快速修复：
 * - 导航到重复项
 */
class DuplicatePropertiesInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitPropertyList(element: ParadoxLocalisationPropertyList) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(containerElement: ParadoxLocalisationPropertyList, holder: ProblemsHolder) {
        val elementGroup = containerElement.findChildren<ParadoxLocalisationProperty>().groupBy { it.name }
        if (elementGroup.isEmpty()) return
        for ((name, elements) in elementGroup) {
            ProgressManager.checkCanceled()
            if (name.isEmpty()) continue
            if (elements.size <= 1) continue
            elements.forEachFast { element ->
                val location = element.propertyKey
                val description = ChronicleInspectionBundle.message("localisation.duplicateProperties.desc", name)
                val fix = NavigateToDuplicatesFix(name, element, elements)
                holder.registerProblem(location, description, fix)
            }
        }
    }
}
