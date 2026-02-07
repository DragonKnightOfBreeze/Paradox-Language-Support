package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.navigation.NavigateToRecursionsFix
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor

/**
 * （对于本地化文件）检查是否存在不支持的递归。
 *
 * - 对于每个本地化，检查其本地化文本中是否存在递归的本地化引用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool(), DumbAware {
    // 目前仅做检查即可，不需要显示递归的装订线图标

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitProperty(element: ParadoxLocalisationProperty) {
                ProgressManager.checkCanceled()
                val name = element.name
                if (name.isEmpty()) return
                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveLocalisation(element, recursions)
                if (recursions.isEmpty()) return
                val location = element.propertyKey
                val description = PlsBundle.message("inspection.localisation.unsupportedRecursion.desc.1")
                holder.registerProblem(location, description, NavigateToRecursionsFix(name, element, recursions))
            }
        }
    }
}
