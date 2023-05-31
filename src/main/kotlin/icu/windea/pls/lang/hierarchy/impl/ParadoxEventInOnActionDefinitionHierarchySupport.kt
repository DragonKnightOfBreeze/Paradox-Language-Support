package icu.windea.pls.lang.hierarchy.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxEventInOnActionDefinitionHierarchySupport: ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "event.in.onAction"
        
        //val containingOnActionNameKey = Key.create<String>("paradox.definition.hierarchy.event.in.event.containingOnActionName") //definitionName
    }
    
    override val id: String = ID
    
    override fun indexData(fileData: MutableMap<String, MutableList<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        if(definitionInfo.type != "on_action") return
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        if(configExpression.value?.substringBefore('.') != "event") return
        
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, element.startOffset, definitionInfo.gameType)
        fileData.getOrPut(id) { mutableListOf() }.add(info)
    }
}

