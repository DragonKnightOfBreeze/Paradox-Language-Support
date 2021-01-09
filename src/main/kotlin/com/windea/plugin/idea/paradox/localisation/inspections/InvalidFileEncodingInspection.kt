package com.windea.plugin.idea.paradox.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.message
import java.nio.charset.*

class InvalidFileEncodingInspection : LocalInspectionTool() {
	companion object{
		private fun _description(charset: Charset, bom: String) = message("paradox.localisation.inspection.invalidFileEncoding.description", charset, bom)
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor?>? {
		val charset = file.virtualFile.charset
		val hasBom = file.virtualFile.bom != null
		val isValid = charset == Charsets.UTF_8 && hasBom
		if(!isValid){
			val holder = ProblemsHolder(manager,file,isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, _description(charset, bom), ChangeFileEncoding(file))
			return holder.resultsArray
		}
		return null
	}
	
	private class ChangeFileEncoding(
		element: PsiElement
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		companion object{
			private val _name = message("paradox.localisation.quickFix.changeFileEncoding")
		}
		
		override fun getFamilyName() = _name

		override fun getText() = _name

		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO 让IDE知道修改bom是对文档进行了修改
			file.virtualFile.charset = Charsets.UTF_8
			file.virtualFile.bom = utf8Bom
			file.virtualFile.refresh(false,false)
			file.subtreeChanged()
		}
	}
}
