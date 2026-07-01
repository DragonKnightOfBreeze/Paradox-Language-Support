package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.fixes.ReplaceStringFix
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.formatter.ParadoxScriptCodeStyleSettings

object ParadoxSyntaxInspectionService {
    fun createContext(holder: ProblemsHolder): ParadoxSyntaxInspectionContext {
        val file = selectFile(holder.file)
        val rootFile = selectRootFile(holder.file)
        val rootInfo = file?.fileInfo?.rootInfo
        val gameType = rootInfo?.gameType
        val gameVersion = rootInfo?.gameVersion
        return ParadoxSyntaxInspectionContext(holder, file, rootFile, gameType, gameVersion)
    }

    fun checkByConstraint(element: PsiElement, context: ParadoxSyntaxInspectionContext, constraint: ParadoxSyntaxConstraint, name: String): Boolean {
        if (context.gameType == null || context.gameType == ParadoxGameType.Core) return true
        val testResult = constraint.testResult(context.gameType, context.gameVersion)
        if (!testResult.strictValue) {
            val description = when {
                testResult.sinceGameVersion == null -> ChronicleBundle.message("incorrectSyntax.desc.in.game", name, context.gameType.title)
                else -> ChronicleBundle.message("incorrectSyntax.desc.since.gameVersion", name, context.gameType.title, testResult.sinceGameVersion)
            }
            val fixes = getFixes(element, context, constraint, testResult)
            context.holder.registerProblem(element, description, *fixes)
            return false
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun getFixes(element: PsiElement, context: ParadoxSyntaxInspectionContext, constraint: ParadoxSyntaxConstraint, testResult: ParadoxSyntaxConstraint.TestResult): Array<LocalQuickFix> {
        if (testResult.strictValue) return LocalQuickFix.EMPTY_ARRAY // 严格匹配 -> 不报错，直接返回
        if (testResult.value) return LocalQuickFix.EMPTY_ARRAY // 游戏版本不匹配，但游戏类型匹配 -> 直接返回
        val result = mutableListOf<LocalQuickFix>()
        when (constraint) {
            ParadoxSyntaxConstraint.SafeAssignOperator -> {
                if (context.gameType != null && ParadoxSyntaxConstraint.SafeCallAssignOperator.testTarget(context.gameType)) {
                    result += getReplaceWithSafeCallAssignOperatorFix(element, context)
                }
                result += getReplaceWithAssignOperatorFix(element, context)
            }
            ParadoxSyntaxConstraint.SafeCallAssignOperator -> {
                if (context.gameType != null && ParadoxSyntaxConstraint.SafeAssignOperator.testTarget(context.gameType)) {
                    result += getReplaceWithSafeAssignOperatorFix(element, context)
                }
                result += getReplaceWithAssignOperatorFix(element, context)
            }
            else -> {}
        }
        if (result.isEmpty()) return LocalQuickFix.EMPTY_ARRAY
        return result.toTypedArray()
    }

    private fun getReplaceWithAssignOperatorFix(element: PsiElement, context: ParadoxSyntaxInspectionContext): ReplaceStringFix {
        val spaceExtendedTextRange = PsiService.getSpaceExtendedTextRange(element)
        val offset = spaceExtendedTextRange.startOffset
        val length = spaceExtendedTextRange.endOffset - offset
        val spaceAroundPropertySeparator = ParadoxScriptCodeStyleSettings.getInstance(context.holder.file).SPACE_AROUND_PROPERTY_SEPARATOR
        val string = if (spaceAroundPropertySeparator) " = " else "="
        val fix = ReplaceStringFix(element, ChronicleBundle.message("incorrectSyntax.safeAssign.fix.1"), string, offset, length)
        return fix
    }

    private fun getReplaceWithSafeCallAssignOperatorFix(element: PsiElement, context: ParadoxSyntaxInspectionContext): ReplaceStringFix {
        val spaceExtendedTextRange = PsiService.getSpaceExtendedTextRange(element)
        val offset = spaceExtendedTextRange.startOffset
        val length = spaceExtendedTextRange.endOffset - offset
        val spaceAroundPropertySeparator = ParadoxScriptCodeStyleSettings.getInstance(context.holder.file).SPACE_AROUND_PROPERTY_SEPARATOR
        val string = if (spaceAroundPropertySeparator) "? = " else "? ="
        val fix = ReplaceStringFix(element, ChronicleBundle.message("incorrectSyntax.safeAssign.fix.2"), string, offset, length)
        return fix
    }

    private fun getReplaceWithSafeAssignOperatorFix(element: PsiElement, context: ParadoxSyntaxInspectionContext): ReplaceStringFix {
        val spaceExtendedTextRange = PsiService.getSpaceExtendedTextRange(element)
        val offset = spaceExtendedTextRange.startOffset
        val length = spaceExtendedTextRange.endOffset - offset
        val spaceAroundPropertySeparator = ParadoxScriptCodeStyleSettings.getInstance(context.holder.file).SPACE_AROUND_PROPERTY_SEPARATOR
        val string = if (spaceAroundPropertySeparator) " ?= " else "?="
        val fix = ReplaceStringFix(element, ChronicleBundle.message("incorrectSyntax.safeAssign.fix.3"), string, offset, length)
        return fix
    }
}
