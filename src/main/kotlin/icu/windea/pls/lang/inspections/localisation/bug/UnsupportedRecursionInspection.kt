package icu.windea.pls.lang.inspections.localisation.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * （对于本地化文件）检查是否存在不支持的递归。
 * * 对于每个本地化，检查其本地化文本中是否存在递归的本地化引用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when (element) {
                    is ParadoxLocalisationProperty -> visitLocalisationProperty(element)
                }
            }

            private fun visitLocalisationProperty(element: ParadoxLocalisationProperty) {
                val name = element.name
                if (name.isEmpty()) return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveLocalisation(element, recursions)
                if (recursions.isEmpty()) return
                val message = PlsBundle.message("inspection.localisation.unsupportedRecursion.desc.1")
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionFix(name, element, recursions))
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFilePathManager.inLocalisationPath(fileInfo.path)
    }

    private class NavigateToRecursionFix(key: String, target: PsiElement, recursions: Collection<PsiElement>) : NavigateToFix(key, target, recursions) {
        override fun getText() = PlsBundle.message("inspection.localisation.unsupportedRecursion.fix.1")

        override fun getPopupTitle(editor: Editor) =
            PlsBundle.message("inspection.localisation.unsupportedRecursion.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement) =
            PlsBundle.message("inspection.localisation.unsupportedRecursion.fix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
    }
}
