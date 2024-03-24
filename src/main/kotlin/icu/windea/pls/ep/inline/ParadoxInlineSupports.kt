package icu.windea.pls.ep.inline

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxInlineScriptInlineSupport : ParadoxInlineSupport {
    //这里需要尝试避免SOE，如果发生SOE，使用发生之前最后得到的那个结果
    
    override fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
        if(element !is ParadoxScriptProperty) return null
        val info = ParadoxInlineScriptHandler.getUsageInfo(element) ?: return null
        val expression = info.expression
        return withRecursionGuard("icu.windea.pls.lang.inline.ParadoxInlineScriptInlineSupport.inlineElement") a1@{
            withCheckRecursion(expression) a2@{
                val configContext = CwtConfigHandler.getConfigContext(element) ?: return@a2 null
                val project = configContext.configGroup.project
                ParadoxInlineScriptHandler.getInlineScriptFile(expression, element, project)
            }
        }
    }
}