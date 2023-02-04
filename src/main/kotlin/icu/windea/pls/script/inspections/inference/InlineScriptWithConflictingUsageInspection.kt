package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 使用位置有冲突的内联脚本的检查。
 */
@WithGameType(ParadoxGameType.Stellaris)
class InlineScriptWithConflictingUsageInspection : LocalInspectionTool(){
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(file !is ParadoxScriptFile) return null
        if(!getSettings().inference.inlineScriptLocation) return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!ParadoxInlineScriptHandler.isGameTypeSupported(gameType)) return null
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return null
        val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(file) ?: return null
        if(usageInfo.hasConflict) {
            val holder = ProblemsHolder(manager, file, isOnTheFly)
            val description = PlsBundle.message("inspection.script.inference.inlineScriptWithConflictingUsage.description", inlineScriptExpression)
            holder.registerProblem(file, description, GotoInlineScriptUsagesIntention())
            return holder.resultsArray
        }
        return null
    }
}

