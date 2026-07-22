package icu.windea.pls.ep.inspections

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.lang.fixes.ReplaceStringFix
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionContext
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionService
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查脚本文件中的比较运算符是否符合期望。
 */
class ParadoxComparisonOperatorChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        if (context.rootFile == null) return true
        if (!ParadoxSyntaxService.isComparisonOperator(element)) return true
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return true

        // check on grammar level

        val allowed = ParadoxSyntaxService.isComparisonOperatorAllowed(propertyElement)
        if (allowed == false) {
            val description = ChronicleEpBundle.message("incorrectSyntax.comparison.desc.1")
            context.holder.registerProblem(element, description)
            return false
        }

        // check on semantic level

        if (context.gameType == null || context.gameType == ParadoxGameType.Core) return true
        if (DumbService.isDumb(context.holder.project)) return true

        val valid = ParadoxSyntaxService.isComparisonOperatorValid(propertyElement)
        if (valid == false) {
            val description = ChronicleEpBundle.message("incorrectSyntax.comparison.desc.2")
            context.holder.registerProblem(element, description)
            return false
        }
        return true
    }
}

/**
 * 检查脚本文件中的安全赋值运算符是否符合期望。
 */
class ParadoxSafeAssignOperatorChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        if (context.rootFile == null) return true
        if (!ParadoxSyntaxService.isSafeAssignOperator(element)) return true
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return true
        checkSafeAssignOperator(element, propertyElement, context).let { if (!it) return false }
        checkSafeCallAssignOperator(element, propertyElement, context).let { if (!it) return false }
        return true
    }

    private fun checkSafeAssignOperator(element: PsiElement, propertyElement: ParadoxScriptProperty, context: ParadoxSyntaxInspectionContext): Boolean {
        if (element.elementType != ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN) return true

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeAssignOperator
        val name = ChronicleBundle.message("incorrectSyntax.desc.safeAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(element, context, constraint, name)) return true

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = ChronicleEpBundle.message("incorrectSyntax.safeAssign.desc.1")
            context.holder.registerProblem(element, description)
            return false
        }

        // check on semantic level

        if (context.gameType == null || context.gameType == ParadoxGameType.Core) return true
        if (DumbService.isDumb(context.holder.project)) return true
        if (!ChronicleFacade.checkConfigGroupInitialized(context.holder.project, context.gameType)) return true

        val configGroup = ChronicleFacade.getConfigGroup(context.gameType)
        val valid = ParadoxSyntaxService.isSafeAssignOperatorValid(propertyElement, configGroup)
        if (!valid) {
            val description = ChronicleEpBundle.message("incorrectSyntax.safeAssign.desc.2")
            context.holder.registerProblem(element, description)
            return false
        }

        return true
    }

    private fun checkSafeCallAssignOperator(element: PsiElement, propertyElement: ParadoxScriptProperty, context: ParadoxSyntaxInspectionContext): Boolean {
        if (element.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) return true

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeCallAssignOperator
        val name = ChronicleBundle.message("incorrectSyntax.desc.safeCallAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(element, context, constraint, name)) return true

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = ChronicleEpBundle.message("incorrectSyntax.safeAssign.desc.3")
            context.holder.registerProblem(element, description)
            return false
        }

        // check on semantic level

        if (context.gameType == null || context.gameType == ParadoxGameType.Core) return true
        if (DumbService.isDumb(context.holder.project)) return true
        if (!ChronicleFacade.checkConfigGroupInitialized(context.holder.project, context.gameType)) return true

        val configGroup = ChronicleFacade.getConfigGroup(context.gameType)
        val valid = ParadoxSyntaxService.isSafeAssignOperatorValid(propertyElement, configGroup)
        if (!valid) {
            val description = ChronicleEpBundle.message("incorrectSyntax.safeAssign.desc.4")
            context.holder.registerProblem(element, description)
            return false
        }

        return true
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
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        checkDanglingColorfulTextEndMarker(element, context).let { if (!it) return false }
        checkDanglingTextFormatEndMarker(element, context).let { if (!it) return false }
        return true
    }

    private fun checkDanglingColorfulTextEndMarker(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        if (!ParadoxSyntaxService.isDanglingColorfulTextEndMarker(element)) return true
        val description = ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.desc.1")
        val fix = DeleteStringByElementTypeFix(element, ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.fix"))
        context.holder.registerProblem(element, description, fix)
        return false
    }

    private fun checkDanglingTextFormatEndMarker(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        if (!ParadoxSyntaxService.isDanglingTextFormatEndMarker(element)) return true
        val description = ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.desc.2")
        val fix = DeleteStringByElementTypeFix(element, ChronicleEpBundle.message("incorrectSyntax.danglingEndMarker.fix"))
        context.holder.registerProblem(element, description, fix)
        return false
    }
}

/**
 * 检查本地化文件中的不正确的对左方括号（[LEFT_BRACKET]）的转义。
 */
class ParadoxLeftBracketEscapeChecker : ParadoxIncorrectSyntaxChecker {
    override fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext): Boolean {
        val indices = ParadoxSyntaxService.getIncorrectLeftBracketEscapeIndices(element, context.holder.file)
        if (indices.isEmpty()) return true
        val description = ChronicleEpBundle.message("incorrectSyntax.leftBracketEscape.desc")
        val startOffset = element.startOffset
        for (index in indices) {
            val rangeInElement = TextRange.from(index, 2)
            val fix = ReplaceStringFix(element, ChronicleEpBundle.message("incorrectSyntax.leftBracketEscape.fix"), "[[", startOffset + index, 2)
            context.holder.registerProblem(element, rangeInElement, description, fix)
        }
        return true // continue check
    }
}
