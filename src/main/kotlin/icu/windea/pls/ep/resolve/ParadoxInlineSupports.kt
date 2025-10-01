package icu.windea.pls.ep.resolve

import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供对内联脚本的内联逻辑的支持。
 *
 * @see ParadoxInlineScriptManager
 */
class ParadoxInlineScriptInlineSupport : ParadoxInlineSupport {
    // 这里需要尝试避免SOE，如果发生SOE，使用发生之前最后得到的那个结果

    override fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptFile? {
        // 排除为空或者带参数的情况
        if (element !is ParadoxScriptProperty) return null
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(element).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        return withRecursionGuard a1@{
            withRecursionCheck(inlineScriptExpression) a2@{
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return@a2 null
                val project = configContext.configGroup.project
                ParadoxInlineScriptManager.getInlineScriptFile(inlineScriptExpression, project, element)
            }
        }
    }
}
