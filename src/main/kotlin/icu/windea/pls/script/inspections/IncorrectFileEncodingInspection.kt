package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.encoding.ChangeFileEncodingAction
import com.intellij.openapi.vfs.encoding.EncodingUtil
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

//com.intellij.openapi.editor.actions.AddBomAction
//com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 不正确的的文件编码的检查。
 *
 * 提供快速修复：
 * * 改为正确的文件编码
 * 
 * 注意：[icu.windea.pls.core.ParadoxFileTypeOverrider]会尝试自动修正文件的BOM。
 */
class IncorrectFileEncodingInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不应该出现
		val virtualFile = file.virtualFile ?: return null
		val charset = virtualFile.charset
		val hasBom = virtualFile.hasBom(utf8Bom)
		val fileInfo = virtualFile.fileInfo ?: return null //无法获取文件信息时跳过检查
		val isNameList = fileInfo.path.parent.startsWith("common/name_lists")
		val isValid = charset == Charsets.UTF_8 && (if(isNameList) hasBom else !hasBom)
		if(!isValid) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, PlsBundle.message("script.inspection.incorrectFileEncoding.description", charset, bom),
				ChangeToCorrectFileEncodingFix(file, isNameList)
			)
			return holder.resultsArray
		}
		return null
	}
	
	private class ChangeToCorrectFileEncodingFix(
		element: PsiElement,
		private val isNameList: Boolean
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getText() = PlsBundle.message("script.inspection.incorrectFileEncoding.fix.1")
		
		override fun getFamilyName() = PlsBundle.message("script.inspection.incorrectFileEncoding.fix.1")
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val virtualFile = file.virtualFile
			val isUtf8 = virtualFile.charset == Charsets.UTF_8
			val hasBom = virtualFile.hasBom(utf8Bom)
			if(isNameList && !hasBom) {
				virtualFile.addBom(utf8Bom)
			} else if(!isNameList && hasBom) {
				virtualFile.removeBom(utf8Bom)
			}
			if(!isUtf8) virtualFile.charset = Charsets.UTF_8
			val fileDocumentManager = FileDocumentManager.getInstance()
			val document = fileDocumentManager.getDocument(virtualFile)
			if(document != null) {
				if(!isUtf8) {
					ChangeFileEncodingAction.changeTo(project, document, editor, virtualFile, Charsets.UTF_8, EncodingUtil.Magic8.ABSOLUTELY, EncodingUtil.Magic8.ABSOLUTELY)
				}
				fileDocumentManager.saveDocument(document) //保存文件
			}
		}
		
		override fun startInWriteAction() = false
	}
}
