package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import java.nio.charset.*

//com.intellij.openapi.editor.actions.AddBomAction
//com.intellij.openapi.editor.actions.RemoveBomAction

private fun _description(charset: Charset, bom: String) = PlsBundle.message("script.inspection.incorrectFileEncoding.description", charset, bom)
private fun _quickFix1Name() = PlsBundle.message("script.inspection.incorrectFileEncoding.quickFix.1")

/**
 * 不正确的的文件编码的检查。
 *
 * 注意：[icu.windea.pls.core.ParadoxFileTypeOverrider]会尝试自动修正文件的BOM。
 *
 * 提供快速修复：
 * * 改为正确的文件编码
 */
class IncorrectFileEncodingInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor?>? {
		val virtualFile = file.virtualFile ?: return null
		val charset = virtualFile.charset
		val hasBom = virtualFile.hasBom(utf8Bom)
		val isNameList = virtualFile.fileInfo?.path?.parent?.startsWith("common/name_lists") ?: false
		val isValid = charset == Charsets.UTF_8 && (if(isNameList) hasBom else !hasBom)
		if(!isValid) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, _description(charset, bom),
				ChangeToCorrectFileEncoding(file, isNameList)
			)
			return holder.resultsArray
		}
		return null
	}
	
	private class ChangeToCorrectFileEncoding(
		element: PsiElement,
		private val isNameList: Boolean
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getText() = _quickFix1Name()
		
		override fun getFamilyName() = _quickFix1Name()
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val virtualFile = file.virtualFile
			virtualFile.charset = Charsets.UTF_8
			val hasBom = virtualFile.hasBom(utf8Bom)
			if(isNameList && !hasBom) {
				virtualFile.addBom(utf8Bom)
			} else if(!isNameList && hasBom) {
				virtualFile.removeBom(utf8Bom)
			}
		}
	}
}
