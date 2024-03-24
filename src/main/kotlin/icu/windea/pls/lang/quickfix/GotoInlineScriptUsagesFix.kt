package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class GotoInlineScriptUsagesFix: IntentionAndQuickFixAction() {
    override fun getName(): String {
        return PlsBundle.message("fix.gotoInlineScriptUsages")
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