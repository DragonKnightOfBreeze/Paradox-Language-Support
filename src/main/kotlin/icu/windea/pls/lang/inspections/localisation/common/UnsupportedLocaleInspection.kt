package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale

/**
 * 不支持的语言环境的代码检查。
 */
class UnsupportedLocaleInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationLocale) visitLocale(element)
            }

            private fun visitLocale(element: ParadoxLocalisationLocale) {
                val localeConfig = selectLocale(element)
                if (localeConfig != null) return
                val location = element.idElement
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unsupportedLocale.desc", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}

