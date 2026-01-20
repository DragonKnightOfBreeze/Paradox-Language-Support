package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.navigation.NavigateToDuplicatesFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * 同一文件中重复的（同一语言环境的）属性声明的代码检查。
 *
 * 提供快速修复：
 * - 导航到重复项
 */
class DuplicatePropertiesInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitPropertyList(element: ParadoxLocalisationPropertyList) {
                ProgressManager.checkCanceled()
                val propertyGroup = element.propertyList.groupBy { it.name }
                if (propertyGroup.isEmpty()) return
                for ((key, values) in propertyGroup) {
                    if (values.size <= 1) continue
                    for (value in values) {
                        // 第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
                        val location = value.propertyKey
                        val fix = NavigateToDuplicatesFix(key, value, values)
                        val description = PlsBundle.message("inspection.localisation.duplicateProperties.desc", key)
                        holder.registerProblem(location, description, fix)
                    }
                }
            }
        }
    }
}
