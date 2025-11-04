package icu.windea.pls.lang.inspections.localisation.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.navigation.NavigateToRecursionsFix
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * （对于本地化文件）检查是否存在不支持的递归。
 * - 对于每个本地化，检查其本地化文本中是否存在递归的本地化引用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool(), DumbAware {
    // 目前仅做检查即可，不需要显示递归的装订线图标

    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationProperty) visitLocalisationProperty(element)
            }

            private fun visitLocalisationProperty(element: ParadoxLocalisationProperty) {
                val name = element.name
                if (name.isEmpty()) return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveLocalisation(element, recursions)
                if (recursions.isEmpty()) return
                val message = PlsBundle.message("inspection.localisation.unsupportedRecursion.desc.1")
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionsFix(name, element, recursions))
            }
        }
    }
}
