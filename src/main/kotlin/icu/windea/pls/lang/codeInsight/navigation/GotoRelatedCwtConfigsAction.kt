package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义成员对应的CWT规则的动作。
 */
class GotoRelatedCwtConfigsAction : BaseCodeInsightAction() {
	private val handler = GotoRelatedCwtConfigsHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return (file is ParadoxScriptFile || file is ParadoxLocalisationFile) && file.fileInfo != null
	}
	
	override fun update(event: AnActionEvent) {
		//possible for any element in script and localisation files (inside game or mod directory)
		//but related CWT configs may not exist
	}
}
