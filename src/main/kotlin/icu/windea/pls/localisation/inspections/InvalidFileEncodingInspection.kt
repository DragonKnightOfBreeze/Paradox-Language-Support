package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import java.nio.charset.*

//com.intellij.openapi.editor.actions.AddBomAction
//com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 非法的文件编码的检查。
 *
 * 注意：[icu.windea.pls.core.ParadoxFileTypeOverrider]会尝试自动修正文件的BOM。
 */
class InvalidFileEncodingInspection : LocalInspectionTool() {
	companion object {
		private fun _description(charset: Charset, bom: String) = PlsBundle.message("localisation.inspection.invalidFileEncoding.description", charset, bom)
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor?>? {
		val virtualFile = file.virtualFile ?: return null
		val charset = virtualFile.charset
		val hasBom = virtualFile.hasBom(utf8Bom)
		val isValid = charset == Charsets.UTF_8 && hasBom
		if(!isValid) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, _description(charset, bom), ChangeFileEncoding(file))
			return holder.resultsArray
		}
		return null
	}
	
	private class ChangeFileEncoding(
		element: PsiElement
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		companion object {
			private val _name = PlsBundle.message("localisation.quickFix.changeFileEncoding")
		}
		
		override fun getFamilyName() = _name
		
		override fun getText() = _name
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val virtualFile = file.virtualFile
			virtualFile.charset = Charsets.UTF_8
			val hasBom = virtualFile.hasBom(utf8Bom)
			if(!hasBom) {
				virtualFile.addBom(utf8Bom)
			}
		}
	}
}
