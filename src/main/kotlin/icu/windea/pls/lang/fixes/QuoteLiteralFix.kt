package icu.windea.pls.lang.fixes

import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle

class QuoteLiteralFix : PsiUpdateModCommandQuickFix(), DumbAware {
    override fun getFamilyName() = ChronicleInspectionBundle.message("fix.quoteLiteral.name")

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        if (element !is PsiLiteralValue) return
        val quotePattern = if (element is PsiQuoteAwareElement) element.quotePattern else QuotePatterns.Default
        ElementManipulators.handleContentChange(element, element.text.quote(quotePattern))
    }
}
