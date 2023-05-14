package icu.windea.pls.lang.inline.impl

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ParadoxInlineScriptHandler.getInlineScriptExpression
import icu.windea.pls.lang.inline.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxInlineScriptScriptMemberElementInlineSupport : ParadoxScriptMemberElementInlineSupport {
    override fun canLink(element: ParadoxScriptMemberElement): Boolean {
        if(element !is ParadoxScriptFile) return false
        if(getInlineScriptExpression(element) == null) return false
        return true
    }
    
    //这里需要尝试避免SOE
    
    override fun linkElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement? {
        if(!getSettings().inference.inlineScriptConfig) return null
        if(element !is ParadoxScriptFile) return null
        val expression = getInlineScriptExpression(element)
        if(expression == null) return null
        return withRecursionGuard {
            withCheckRecursion(expression) {
                val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(element) ?: return null
                if(usageInfo.hasConflict) return null
                usageInfo.pointer.element
            }
        }
    }
    
    override fun inlineElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement? {
        if(element !is ParadoxScriptProperty) return null
        val info = ParadoxInlineScriptHandler.getInfo(element) ?: return null
        val expression = info.expression
        return withRecursionGuard { 
            withCheckRecursion(expression) {
                val definitionMemberInfo = element.definitionMemberInfo
                if(definitionMemberInfo == null) return null
                val project = definitionMemberInfo.configGroup.project
                ParadoxInlineScriptHandler.getInlineScript(expression, element, project)
            }
        }
    }
}