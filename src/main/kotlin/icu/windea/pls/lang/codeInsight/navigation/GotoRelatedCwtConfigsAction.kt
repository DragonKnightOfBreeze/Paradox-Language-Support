package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
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
    
	override fun update(event: AnActionEvent) {
        //possible for any element in script and localisation files (inside game or mod directory)
        //but related CWT configs may not exist
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if(file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return
        if(file.fileInfo == null) return
	}
}
