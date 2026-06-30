package icu.windea.pls.ep.inspections

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.PlsEpBundle
import icu.windea.pls.lang.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.lang.fixes.ReplaceStringFix
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionContext
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionService
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查脚本文件中的比较运算符是否符合期望。
 */
class ParadoxComparisonOperatorChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        if (context.rootFile == null) return
        if (!ParadoxSyntaxService.isComparisonOperator(element)) return
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return

        // check on grammar level

        val allowed = ParadoxSyntaxService.isComparisonOperatorAllowed(propertyElement)
        if (allowed == false) {
            val description = PlsEpBundle.message("incorrectSyntax.comparison.desc.1")
            context.holder.registerProblem(element, description)
            return
        }

        // check on semantic level

        if (DumbService.isDumb(context.holder.project)) return

        val valid = ParadoxSyntaxService.isComparisonOperatorValid(propertyElement)
        if (valid == false) {
            val description = PlsEpBundle.message("incorrectSyntax.comparison.desc.2")
            context.holder.registerProblem(element, description)
            return
        }
    }
}

/**
 * 检查脚本文件中的安全赋值运算符是否符合期望。
 */
class ParadoxSafeAssignOperatorChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        if (context.rootFile == null) return
        if (!ParadoxSyntaxService.isSafeAssignOperator(element)) return
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        checkSafeAssignOperator(element, propertyElement, context)
        checkSafeCallAssignOperator(element, propertyElement, context)
    }

    private fun checkSafeAssignOperator(element: PsiElement, propertyElement: ParadoxScriptProperty, context: ParadoxSyntaxInspectionContext) {
        if (element.elementType != ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN) return

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeAssignOperator
        val name = PlsBundle.message("incorrectSyntax.desc.safeAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(element, context, constraint, name)) return

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = PlsEpBundle.message("incorrectSyntax.safeAssign.desc.1")
            context.holder.registerProblem(element, description)
            return
        }

        // check on semantic level

        val configGroup = PlsFacade.getConfigGroup(context.gameType)
        val valid = ParadoxSyntaxService.isSafeAssignOperatorValid(propertyElement, configGroup)
        if (!valid) {
            val description = PlsEpBundle.message("incorrectSyntax.safeAssign.desc.2")
            context.holder.registerProblem(element, description)
            return
        }
    }

    private fun checkSafeCallAssignOperator(element: PsiElement, propertyElement: ParadoxScriptProperty, context: ParadoxSyntaxInspectionContext) {
        if (element.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) return

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeCallAssignOperator
        val name = PlsBundle.message("incorrectSyntax.desc.safeCallAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(element, context, constraint, name)) return

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = PlsEpBundle.message("incorrectSyntax.safeAssign.desc.3")
            context.holder.registerProblem(element, description)
            return
        }

        // check on semantic level

        val configGroup = PlsFacade.getConfigGroup(context.gameType)
        val valid = ParadoxSyntaxService.isSafeAssignOperatorValid(propertyElement, configGroup)
        if (!valid) {
            val description = PlsEpBundle.message("incorrectSyntax.safeAssign.desc.4")
            context.holder.registerProblem(element, description)
            return
        }
    }
}

/**
 * 检查本地化文件中的悬挂的富文本结束标记。
 *
 * 包括：
 * - 悬挂的彩色文本（[COLORFUL_TEXT]）的结束标记（[COLORFUL_TEXT_END]，`§!`）。
 * - 悬挂的文本格式（[TEXT_FORMAT]）的结束标记（[TEXT_FORMAT_END]，`#!`）。
 */
class ParadoxDanglingRichTextEndMarkerChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        checkDanglingColorfulTextEndMarker(element, context)
        checkDanglingTextFormatEndMarker(element, context)
    }

    private fun checkDanglingColorfulTextEndMarker(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        if (!ParadoxSyntaxService.isDanglingColorfulTextEndMarker(element)) return
        val description = PlsEpBundle.message("incorrectSyntax.danglingEndMarker.desc.1")
        val fix = DeleteStringByElementTypeFix(element, PlsEpBundle.message("incorrectSyntax.danglingEndMarker.fix"))
        context.holder.registerProblem(element, description, fix)
    }

    private fun checkDanglingTextFormatEndMarker(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        if (!ParadoxSyntaxService.isDanglingTextFormatEndMarker(element)) return
        val description = PlsEpBundle.message("incorrectSyntax.danglingEndMarker.desc.2")
        val fix = DeleteStringByElementTypeFix(element, PlsEpBundle.message("incorrectSyntax.danglingEndMarker.fix"))
        context.holder.registerProblem(element, description, fix)
    }
}

/**
 * 检查本地化文件中的不正确的对左方括号（[LEFT_BRACKET]）的转义。
 */
class ParadoxLeftBracketEscapeChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext) {
        val indices = ParadoxSyntaxService.getIncorrectLeftBracketEscapeIndices(element, context.holder.file)
        if (indices.isEmpty()) return
        val description = PlsEpBundle.message("incorrectSyntax.leftBracketEscape.desc")
        val startOffset = element.startOffset
        for (index in indices) {
            val rangeInElement = TextRange.from(index, 2)
            val fix = ReplaceStringFix(element, PlsEpBundle.message("incorrectSyntax.leftBracketEscape.fix"), "[[", startOffset + index, 2)
            context.holder.registerProblem(element, rangeInElement, description, fix)
        }
    }
}
