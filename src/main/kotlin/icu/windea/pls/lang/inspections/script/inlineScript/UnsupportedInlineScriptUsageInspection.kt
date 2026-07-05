package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否在不支持的地方使用了内联脚本。
 */
class UnsupportedInlineScriptUsageInspection : InlineScriptInspectionBase()/*, DumbAware*/ {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val extension = holder.file.name.substringAfterLast('.').lowercase()
        val isAssetFile = extension == "asset"

        // fast return
        val fastReturn = !isAssetFile
        if (fastReturn) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                checkInAssetFile(element, holder)
            }
        }
    }

    private fun checkInAssetFile(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        if (!ParadoxInlineScriptManager.isMatched(element.name)) return
        val description = ChronicleBundle.message("inspection.script.unsupportedInlineScriptUsage.desc.1")
        holder.registerProblem(element.propertyKey, description)
    }
}
