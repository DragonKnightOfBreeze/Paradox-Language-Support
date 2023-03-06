package icu.windea.pls.lang.support

import icu.windea.pls.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ParadoxInlineScriptHandler.getInlineScriptExpression
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxInlineScriptScriptMemberElementInlineSupport : ParadoxScriptMemberElementInlineSupport {
    override fun canLink(element: ParadoxScriptMemberElement): Boolean {
        if(element !is ParadoxScriptFile) return false
        if(getInlineScriptExpression(element) == null) return false
        return true
    }
    
    override fun linkElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
        if(!getSettings().inference.inlineScriptLocation) return null
        if(element !is ParadoxScriptFile) return null
        if(getInlineScriptExpression(element) == null) return null
        val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(element) ?: return null
        if(usageInfo.hasConflict) return null
        return usageInfo.pointer.element
    }
    
    override fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
        if(element !is ParadoxScriptProperty) return null
        val name = element.name.lowercase()
        if(name != "inline_script") return null
        val definitionMemberInfo = element.definitionMemberInfo
        if(definitionMemberInfo == null) return null
        //这时就可以确定这个element确实匹配规则inline[inline_script]了
        val info = ParadoxInlineScriptHandler.getInfo(element.propertyKey) ?: return null
        val expression = info.expression
        val project = definitionMemberInfo.configGroup.project
        return ParadoxInlineScriptHandler.getInlineScript(expression, element, project)
    }
}