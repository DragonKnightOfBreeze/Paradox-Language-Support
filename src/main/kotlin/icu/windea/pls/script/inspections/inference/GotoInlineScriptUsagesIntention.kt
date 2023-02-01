package icu.windea.pls.script.inspections.inference

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class GotoInlineScriptUsagesIntention: IntentionAndQuickFixAction() {
    override fun getName(): String {
        return PlsBundle.message("script.intention.gotoInlineScriptUsages")
    }
    
    override fun getFamilyName(): String {
        return name
    }
    
    override fun applyFix(project: Project, file: PsiFile?, editor: Editor?) {
        if(file == null || editor == null) return
        if(file !is ParadoxScriptFile) return
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(file)
        if(expression == null) return
        GotoDeclarationAction.startFindUsages(editor, project, file)
    }
}