package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeStyle.PlsCodeStyleUtil
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.quickfix.ReplaceStringFix
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

object ParadoxSyntaxInspectionService {
    fun createContext(holder: ProblemsHolder): ParadoxSyntaxInspectionContext {
        val file = selectFile(holder.file)
        val rootFile = selectRootFile(holder.file)
        val rootInfo = file?.fileInfo?.rootInfo
        val gameType = rootInfo?.gameType
        val gameVersion = rootInfo?.gameVersion
        return ParadoxSyntaxInspectionContext(holder, file, rootFile, gameType, gameVersion)
    }

    fun checkByConstraint(context: ParadoxSyntaxInspectionContext, element: PsiElement, constraint: ParadoxSyntaxConstraint, name: String): Boolean {
        if (context.gameType == null || context.gameType == ParadoxGameType.Core) return true
        val testResult = constraint.testResult(context.gameType, context.gameVersion)
        if (!testResult.strictValue) {
            val description = when {
                testResult.sinceGameVersion == null -> PlsBundle.message("snippet.syntax.in.game", name, context.gameType.title)
                else -> PlsBundle.message("snippet.syntax.since.gameVersion", name, context.gameType.title, testResult.sinceGameVersion)
            }
            val fixes = getFixes(context, element, constraint, testResult)
            context.holder.registerProblem(element, description, *fixes)
            return false
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun getFixes(context: ParadoxSyntaxInspectionContext, element: PsiElement, constraint: ParadoxSyntaxConstraint, testResult: ParadoxSyntaxConstraint.TestResult): Array<LocalQuickFix> {
        if (testResult.strictValue) return LocalQuickFix.EMPTY_ARRAY
        if (testResult.value) return LocalQuickFix.EMPTY_ARRAY // 游戏版本不匹配，但游戏类型匹配 -> 直接返回
        val result = mutableListOf<LocalQuickFix>()
        when (constraint) {
            ParadoxSyntaxConstraint.SafeAssignOperator -> {
                val startElement = element.siblings(forward = false).takeWhile { it === element || it is PsiWhiteSpace }.last()
                val endElement = element.siblings(forward = true).takeWhile { it === element || it is PsiWhiteSpace }.last()
                val spaceAroundPropertySeparator = PlsCodeStyleUtil.isSpaceAroundPropertySeparator(context.holder.file)
                val offset = startElement.startOffset
                val length = endElement.endOffset - offset
                val string = if(spaceAroundPropertySeparator) "? = " else "? ="
                result += ReplaceStringFix(element, PlsBundle.message("inspection.script.incorrectSyntax.fix.1.name"), string, offset, length)
            }
            ParadoxSyntaxConstraint.SafeCallAssignOperator -> {
                val startElement = element.siblings(forward = false).takeWhile { it === element || it is PsiWhiteSpace }.last()
                val endElement = element.siblings(forward = true).takeWhile { it === element || it is PsiWhiteSpace }.last()
                val spaceAroundPropertySeparator = PlsCodeStyleUtil.isSpaceAroundPropertySeparator(context.holder.file)
                val offset = startElement.startOffset
                val length = endElement.endOffset - offset
                val string = if(spaceAroundPropertySeparator) " ?= " else "?="
                result += ReplaceStringFix(element, PlsBundle.message("inspection.script.incorrectSyntax.fix.2.name"), string, offset, length)
            }
            else -> {}
        }
        return result.toTypedArray()
    }
}
