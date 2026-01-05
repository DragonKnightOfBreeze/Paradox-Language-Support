package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查是否在不支持的地方使用了内联脚本。
 */
class UnsupportedInlineScriptUsageInspection : InlineScriptInspectionBase()/*, DumbAware*/ {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val extension = holder.file.name.substringAfterLast('.').lowercase()
        if (extension == "asset") {
            return object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element !is ParadoxScriptProperty) return
                    if (!ParadoxInlineScriptManager.isMatched(element.name)) return
                    val description = PlsBundle.message("inspection.script.unsupportedInlineScriptUsage.desc.1")
                    holder.registerProblem(element.propertyKey, description)
                }
            }
        }
        return PsiElementVisitor.EMPTY_VISITOR
    }
}
