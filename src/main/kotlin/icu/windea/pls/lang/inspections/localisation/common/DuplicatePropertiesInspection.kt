package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.localisation.psi.*
import org.jetbrains.annotations.*

/**
 * 同一文件中重复的（同一语言区域的）属性声明的检查。
 *
 * 提供快速修复：
 * * 导航到重复项
 */
class DuplicatePropertiesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationPropertyList) visitPropertyList(element)
            }

            private fun visitPropertyList(element: ParadoxLocalisationPropertyList) {
                ProgressManager.checkCanceled()
                val propertyGroup = element.propertyList.groupBy { it.name }
                if (propertyGroup.isEmpty()) return
                for ((key, values) in propertyGroup) {
                    if (values.size <= 1) continue
                    for (value in values) {
                        //第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
                        val location = value.propertyKey
                        val fix = NavigateToDuplicatesFix(key, value, values)
                        val message = PlsBundle.message("inspection.localisation.duplicateProperties.desc", key)
                        holder.registerProblem(location, message, fix)
                    }
                }
            }
        }
    }

    private class NavigateToDuplicatesFix(key: String, element: PsiElement, duplicates: Collection<PsiElement>) : NavigateToFix(key, element, duplicates, true) {
        override fun getText() = PlsBundle.message("inspection.localisation.duplicateProperties.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.localisation.duplicateProperties.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): @Nls String {
            val lineNumber = editor.document.getLineNumber(value.textOffset)
            return PlsBundle.message("inspection.fix.navigate.popup.text.2", key, lineNumber)
        }
    }
}
