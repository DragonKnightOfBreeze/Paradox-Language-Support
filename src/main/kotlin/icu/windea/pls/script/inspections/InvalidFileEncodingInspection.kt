package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import java.nio.charset.*

class InvalidFileEncodingInspection: LocalInspectionTool(){
	companion object{
		private fun _description(charset: Charset,bom:String) = message("paradox.script.inspection.invalidFileEncoding.description", charset,bom)
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor?>? {
		val virtualFile = file.virtualFile?:return null
		val charset = virtualFile.charset
		val hasBom = virtualFile.bom.let{ it != null && it contentEquals utf8Bom  }
		val isNameList = virtualFile.fileInfo?.path?.root == "name_lists"
		val isValid = charset == Charsets.UTF_8 && (if(isNameList) hasBom else !hasBom)
		if(!isValid){
			val holder = ProblemsHolder(manager,file,isOnTheFly)
			val bom = if(hasBom) "BOM" else "NO BOM"
			holder.registerProblem(file, _description(charset,bom), ChangeFileEncoding(file, isNameList))
			return holder.resultsArray
		}
		return null
	}

	private class ChangeFileEncoding(
		element: PsiElement,
		private val isNameList:Boolean
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		companion object{
			private val _name = message("paradox.script.quickFix.changeFileEncoding")
		}
		
		override fun getText() = _name

		override fun getFamilyName() = _name

		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO 让IDE知道修改bom是对文档进行了修改
			val virtualFile = file.virtualFile
			virtualFile.charset = Charsets.UTF_8
			if(isNameList) virtualFile.bom = utf8Bom else virtualFile.bom = null
		}
	}
}
