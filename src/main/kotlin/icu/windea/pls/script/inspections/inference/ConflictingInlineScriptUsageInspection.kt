package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 检查内联脚本的使用位置是否存在冲突。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ConflictingInlineScriptUsageInspection : LocalInspectionTool(){
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(file !is ParadoxScriptFile) return null
        if(!getSettings().inference.inlineScriptConfig) return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!ParadoxInlineScriptHandler.isGameTypeSupported(gameType)) return null
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return null
        val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(file) ?: return null
        if(usageInfo.hasConflict) {
            val holder = ProblemsHolder(manager, file, isOnTheFly)
            val description = PlsBundle.message("script.annotator.inlineScript.conflict", inlineScriptExpression)
            holder.registerProblem(file, description, GotoInlineScriptUsagesIntention())
            return holder.resultsArray
        }
        return null
    }
}
