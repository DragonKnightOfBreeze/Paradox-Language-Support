package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.navigation.NavigateToDuplicatesFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 同一文件中重复的（同一语言环境的）属性声明的代码检查。
 *
 * 提供快速修复：
 * - 导航到重复项
 */
class DuplicatePropertiesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationPropertyList) visitPropertyList(element)
            }

            private fun visitPropertyList(element: ParadoxLocalisationPropertyList) {
                val propertyGroup = element.propertyList.groupBy { it.name }
                if (propertyGroup.isEmpty()) return
                for ((key, values) in propertyGroup) {
                    if (values.size <= 1) continue
                    for (value in values) {
                        // 第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
                        val location = value.propertyKey
                        val fix = NavigateToDuplicatesFix(key, value, values)
                        val message = PlsBundle.message("inspection.localisation.duplicateProperties.desc", key)
                        holder.registerProblem(location, message, fix)
                    }
                }
            }
        }
    }
}
