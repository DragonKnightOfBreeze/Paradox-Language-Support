package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.ParadoxPathMatcher
import icu.windea.pls.model.matches

/**
 * 不支持的语言区域的检查。
 */
class UnsupportedLocaleInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.matches(ParadoxPathMatcher.InLocalisationPath)
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

