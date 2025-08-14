package icu.windea.pls.ep.inline

import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxInlineScriptInlineSupport : ParadoxInlineSupport {
    //这里需要尝试避免SOE，如果发生SOE，使用发生之前最后得到的那个结果

    override fun getInlinedElement(element: ParadoxScriptMemberElement): ParadoxScriptFile? {
        if (element !is ParadoxScriptProperty) return null
        val info = ParadoxInlineScriptManager.getUsageInfo(element) ?: return null
        val expression = info.expression
        return withRecursionGuard a1@{
            withRecursionCheck(expression) a2@{
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return@a2 null
                val project = configContext.configGroup.project
                ParadoxInlineScriptManager.getInlineScriptFile(expression, element, project)
            }
        }
    }
}
