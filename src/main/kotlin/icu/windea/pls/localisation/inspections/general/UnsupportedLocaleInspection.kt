package icu.windea.pls.localisation.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 不支持的语言区域的检查。
 */
class UnsupportedLocaleInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationLocale) visitLocale(element)
            }
            
            private fun visitLocale(element: ParadoxLocalisationLocale) {
                ProgressManager.checkCanceled()
                val localeConfig = selectLocale(element)
                if(localeConfig != null) return
                val location = element.localeId
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.general.unsupportedLocale.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}

