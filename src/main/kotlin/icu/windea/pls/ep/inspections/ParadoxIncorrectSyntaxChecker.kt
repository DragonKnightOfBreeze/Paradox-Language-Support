package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.lang.inspections.ParadoxSyntaxInspectionContext

/**
 * 不正确的语法的检查器。
 *
 * 说明：
 * - 适用于脚本文件和本地化文件中的各种（未被归类到注解器或者其他代码检查的）语法。
 * - 某些语法在词法分析（lexical analysis）阶段和语法分析（syntax analysis）阶段可能采用更宽松的分析策略，在检查阶段才会通过注解器和代码检查报告更多问题。
 * - 例如，不期望的运算符、悬挂的富文本结束标记等。
 * - 检查逻辑需要兼容 dumb mode，因为对应的代码检查实现了 [DumbAware] 接口。
 * - 检查逻辑可能执行于游戏类型级别、文法级别或语义级别。
 *
 * @see icu.windea.pls.lang.inspections.script.common.IncorrectSyntaxInspection
 * @see icu.windea.pls.lang.inspections.localisation.common.IncorrectSyntaxInspection
 */
@WithGameTypeEP
interface ParadoxIncorrectSyntaxChecker: DumbAware {
    fun check(element: PsiElement, context: ParadoxSyntaxInspectionContext)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxIncorrectSyntaxChecker>("icu.windea.pls.incorrectSyntaxChecker")
    }
}
