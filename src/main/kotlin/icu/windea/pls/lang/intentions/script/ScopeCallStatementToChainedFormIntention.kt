package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 将嵌套作用域调用转换为链式形式。
 *
 * 检测于语义级别（需要规则分组数据已初始化）。
 *
 * 说明：
 * - 保留块中内层属性前后的注释，转换后放到前面。
 * - 光标位置需要放到外层属性的 propertyKey 上。
 *
 * ```paradox_script
 * # before
 * root = {
 *     # comment
 *     owner = { a = 1 }
 * }
 *
 * # after
 * # comment
 * root.owner = { a = 1 }
 * ```
 *
 * @see ParadoxScopeCallStatementManipulationService
 */
@Suppress("UnstableApiUsage")
class ScopeCallStatementToChainedFormIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.scopeCallStatementToChainedForm")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        return ParadoxScopeCallStatementManipulationService.convertToChainedForm(element, context.project)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        // 检查光标是否在外层属性的 propertyKey 上
        val offset = context.offset
        val textRange = element.propertyKey.textRange
        if (offset !in textRange.startOffset..<textRange.endOffset) return false
        return ParadoxScopeCallStatementManipulationService.canConvertToChainedForm(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
