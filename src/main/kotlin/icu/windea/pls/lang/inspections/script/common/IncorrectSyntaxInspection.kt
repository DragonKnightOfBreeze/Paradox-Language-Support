package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionContext
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionService
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * （对于脚本文件）检查是否存在不正确的语法。
 *
 * 包括：
 * - 不期望的比较运算符。语法级别和语义级别。
 * - 不受游戏类型支持的安全（调用）赋值运算符。
 * - 不期望的安全（调用）赋值运算符。语法级别。
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = ParadoxSyntaxInspectionService.createContext(holder)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkComparisonOperator(context, element)
                checkSafeAssignOperator(context, element)
                checkSafeCallAssignOperator(context, element)
            }
        }
    }

    private fun checkComparisonOperator(context: ParadoxSyntaxInspectionContext, element: PsiElement) {
        if (context.rootFile == null) return
        if (element.elementType !in ParadoxScriptTokenSets.COMPARISON_OPERATOR_TOKENS) return
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return

        // check on grammar level

        // 所在属性的键与值应可以表示一个数值
        val allowed = ParadoxSyntaxService.isComparisonOperatorAllowed(propertyElement)
        if (allowed == false) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.1")
            context.holder.registerProblem(element, description)
            return
        }

        // check on semantic level

        if (DumbService.isDumb(context.holder.project)) return

        // NOTE 2.1.4 所在属性对应的匹配的规则，不一定在触发器子句规则之内
        // 所在属性对应的匹配的规则，其使用的属性分隔符应是 `==`（而非常规的 `=`）
        val valid = ParadoxSyntaxService.isComparisonOperatorStrictValid(propertyElement)
        if (valid == false) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.2")
            context.holder.registerProblem(element, description)
            return
        }
    }

    private fun checkSafeAssignOperator(context: ParadoxSyntaxInspectionContext, element: PsiElement) {
        if (context.rootFile == null) return
        if (element.elementType != ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN) return
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeAssignOperator
        val name = PlsBundle.message("snippet.syntax.safeAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(context, element, constraint, name)) return

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.3")
            context.holder.registerProblem(element, description)
            return
        }
    }

    private fun checkSafeCallAssignOperator(context: ParadoxSyntaxInspectionContext, element: PsiElement) {
        if (context.rootFile == null) return
        if (element.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) return
        val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return

        // check game type

        val constraint = ParadoxSyntaxConstraint.SafeCallAssignOperator
        val name = PlsBundle.message("snippet.syntax.safeCallAssignOperators")
        if (!ParadoxSyntaxInspectionService.checkByConstraint(context, element, constraint, name)) return

        // check on grammar level

        // 所在属性的键的表达式类型是字符串
        val allowed = ParadoxSyntaxService.isSafeAssignOperatorAllowed(propertyElement)
        if (!allowed) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.4")
            context.holder.registerProblem(element, description)
            return
        }
    }
}
