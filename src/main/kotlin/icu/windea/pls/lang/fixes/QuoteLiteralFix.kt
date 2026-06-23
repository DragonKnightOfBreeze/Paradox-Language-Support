package icu.windea.pls.lang.fixes

import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.quote

class QuoteLiteralFix : PsiUpdateModCommandQuickFix() {
    override fun getFamilyName() = PlsBundle.message("fix.quoteLiteral.name")

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        // no additional checks are needed here
        ElementManipulators.handleContentChange(element, element.text.quote())
    }
}
