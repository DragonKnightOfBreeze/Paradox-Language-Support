package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.encoding.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

//com.intellij.openapi.editor.actions.AddBomAction
//com.intellij.openapi.editor.actions.RemoveBomAction

/**
 * 不正确的的文件编码的检查。
 *
 * 提供快速修复：
 * * 改为正确的文件编码
 * 
 * @see icu.windea.pls.lang.ParadoxUtf8BomOptionProvider
 */
class IncorrectFileEncodingInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不期望的结果
		val virtualFile = file.virtualFile ?: return null
		val charset = virtualFile.charset
		val hasBom = virtualFile.hasBom(PlsConstants.utf8Bom)
		val fileInfo = virtualFile.fileInfo ?: return null //无法获取文件信息时跳过检查
		val isNameList = fileInfo.path.parent.startsWith("common/name_lists")
		val isValid = charset == Charsets.UTF_8 && (if(isNameList) hasBom else !hasBom)
		if(!isValid) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, PlsBundle.message("inspection.script.incorrectFileEncoding.desc", charset, bom),
				ChangeToCorrectFileEncodingFix(file, isNameList)
			)
			return holder.resultsArray
		}
		return null
	}
	
	private class ChangeToCorrectFileEncodingFix(
		element: PsiElement,
		private val isNameList: Boolean
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
		override fun getText() = PlsBundle.message("inspection.script.incorrectFileEncoding.fix.1")
		
		override fun getFamilyName() = text
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			val virtualFile = file.virtualFile
			val isUtf8 = virtualFile.charset == Charsets.UTF_8
			val hasBom = virtualFile.hasBom(PlsConstants.utf8Bom)
			if(isNameList && !hasBom) {
				try {
					virtualFile.addBom(PlsConstants.utf8Bom)
				} catch(e: Exception) {
					thisLogger().warn("Unexpected exception occurred on attempt to add BOM from file $this", e)
				}
			} else if(!isNameList && hasBom) {
				try {
					virtualFile.removeBom(PlsConstants.utf8Bom)
				} catch(e: Exception) {
					thisLogger().warn("Unexpected exception occurred on attempt to remove BOM from file $this", e)
				}
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
		
		override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY
		
		override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
		
		override fun startInWriteAction() = false
	}
}
