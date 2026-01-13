package icu.windea.pls.ep.resolve

import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
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
    override fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptFile? {
        // 排除为空或者带参数的情况
        if (element !is ParadoxScriptProperty) return null
        val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return null
        val project = configContext.configGroup.project
        return ParadoxInlineScriptManager.getInlineScriptFile(inlineScriptExpression, project, element)
    }
}
