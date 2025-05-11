package icu.windea.pls.lang.inspections.script.bug

import cn.yiiguxing.plugin.translate.util.elementType
import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数。
 */
class UnsupportedParameterUsageInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkGeneral(element, holder)
                checkInlineScript(element, holder)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    private fun checkGeneral(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxParameter && element !is ParadoxConditionParameter) return
        if (element.reference?.resolve() != null) return
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterUsage.desc.1"))
    }

    private fun checkInlineScript(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxScriptParameter) return
        if (element.defaultValue == null) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        val fix = DeleteDefaultValueFix(element)
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterUsage.desc.2"), fix)
    }

    private class DeleteDefaultValueFix(
        element: PsiElement
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
        override fun getText() = PlsBundle.message("inspection.script.unsupportedParameterUsage.fix.1")

        override fun getFamilyName() = text

        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            val element = startElement.castOrNull<ParadoxScriptParameter>() ?: return
            val e1 = element.findChild { it.elementType == ParadoxScriptElementTypes.PIPE } ?: return
            val e2 = element.findChild(forward = false) { it.elementType == ParadoxScriptElementTypes.PARAMETER_END } ?: return
            element.deleteChildRange(e1, e2)
        }
    }
}
