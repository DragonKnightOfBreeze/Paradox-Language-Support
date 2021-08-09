package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class MissingEventNamespaceInspection : LocalInspectionTool() {
	companion object {
		private val _description = message("script.inspection.missingEventNamespace.description")
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val rootPath = file.fileInfo?.path?.root?:return null
		if(rootPath != "events") return null //如果不是事件的脚本文件，不做检查
		val block = file.block ?: return null
		val firstProperty = PsiTreeUtil.findChildOfType(block, ParadoxScriptProperty::class.java)
		//判断第一个属性是否名为"namespace"，忽略大小写（如果名为"namespace"但未完成，视为通过检查）
		if(firstProperty == null || !firstProperty.name.equals("namespace", true)) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			holder.registerProblem(file, _description)
			return holder.resultsArray
		}
		return null
	}
}