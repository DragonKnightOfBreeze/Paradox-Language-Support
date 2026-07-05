package icu.windea.pls.lang.fixes

import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.quote

class QuoteLiteralFix : PsiUpdateModCommandQuickFix() {
    override fun getFamilyName() = ChronicleBundle.message("fix.quoteLiteral.name")

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        if (element !is PsiLiteralValue) return
        ElementManipulators.handleContentChange(element, element.text.quote())
    }
}
