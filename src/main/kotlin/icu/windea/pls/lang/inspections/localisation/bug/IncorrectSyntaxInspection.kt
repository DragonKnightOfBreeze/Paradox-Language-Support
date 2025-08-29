package icu.windea.pls.lang.inspections.localisation.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.DeleteStringByElementTypeFix
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_END

class IncorrectSyntaxInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                checkDanglingColorfulTextEndMarker(element)
                checkDanglingTextFormatEndMarker(element)
            }

            private fun checkDanglingColorfulTextEndMarker(element: PsiElement) {
                if (element.elementType != COLORFUL_TEXT_END) return
                if (element.nextSibling == null && element.parent?.elementType == COLORFUL_TEXT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.1")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1", element.text))
                holder.registerProblem(element, message, fix)
            }

            private fun checkDanglingTextFormatEndMarker(element: PsiElement) {
                if (element.elementType != TEXT_FORMAT_END) return
                if (element.nextSibling == null && element.parent?.elementType == TEXT_FORMAT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.2")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1", element.text))
                holder.registerProblem(element, message, fix)
            }
        }
    }
}
