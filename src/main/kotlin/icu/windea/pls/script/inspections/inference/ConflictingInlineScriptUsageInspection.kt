package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*

/**
 * 检查内联脚本的使用位置是否存在冲突。
 */
class ConflictingInlineScriptUsageInspection : LocalInspectionTool(){
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(!getSettings().inference.inlineScriptConfig) return null
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return null
        val configContext = CwtConfigHandler.getConfigContext(file) ?: return null
        if(configContext.inlineScriptHasConflict != true) return null
        
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("script.annotator.inlineScript.conflict", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix())
        return holder.resultsArray
    }
}
