package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxFilePathManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

/**
 * 无法解析的文本格式的检查。
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3)
class UnresolvedTextFormatInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationTextFormat) visitIcon(element)
            }

            private fun visitIcon(element: ParadoxLocalisationTextFormat) {
                val iconName = element.name ?: return
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedTextFormat.desc", iconName), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file)) return false
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFilePathManager.inLocalisationPath(fileInfo.path)
    }
}
