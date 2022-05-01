package icu.windea.pls.core.quickFix

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*

class ImportGameOrModDirectory(
	element: PsiElement
) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
	
	override fun getFamilyName() = PlsBundle.message("core.quickFix.importGameOrModDirectory")
	
	override fun getText() = PlsBundle.message("core.quickFix.importGameOrModDirectory")
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		//TODO
	}
}