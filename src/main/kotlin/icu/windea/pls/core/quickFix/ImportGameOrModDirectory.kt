package icu.windea.pls.core.quickFix

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.inspections.*

private fun _name() = PlsBundle.message("core.quickFix.importGameOrModDirectory")

class ImportGameOrModDirectory(
	element: PsiElement
): LocalQuickFixAndIntentionActionOnPsiElement(element){
	
	override fun getFamilyName() = _name()
	
	override fun getText() = _name()
	
	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
		//TODO
	}
}