package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * （脚本文件中的）不支持的内联数学块的代码检查。
 *
 * 规则如下：
 * - 不支持在资源文件中使用内联数学块，除非是基于 Jomini 的游戏类型。
 */
class UnsupportedInlineMathInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val gameType = selectGameType(holder.file)
        val isJominiBased = gameType matchesBy ParadoxGameTypeConstraint.JominiBased

        val extension = holder.file.name.substringAfterLast('.').lowercase()
        val isAssetFile = extension == "asset"

        // fast return
        val fastReturn = !isAssetFile || isJominiBased
        if (fastReturn) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxScriptVisitor() {
            override fun visitInlineMath(element: ParadoxScriptInlineMath) {
                ProgressManager.checkCanceled()
                if (isAssetFile) checkInAssetFile(element, holder)
            }
        }
    }

    private fun checkInAssetFile(element: ParadoxScriptInlineMath, holder: ProblemsHolder) {
        holder.registerProblem(element, ChronicleBundle.message("inspection.script.unsupportedInlineMath.desc.1"))
    }
}
