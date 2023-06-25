package icu.windea.pls.script.inspections.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.impl.*

/**
 * 检查内联脚本的使用位置是否存在递归。
 */
class RecursiveInlineScriptUsageInspection: LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(!getSettings().inference.inlineScriptConfig) return null
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxConfigHandler.getConfigContext(file) ?: return null
        if(configContext.inlineScriptHasRecursion != true) return null
        
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("script.annotator.inlineScript.recursive", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesIntention())
        return holder.resultsArray
    }
}
