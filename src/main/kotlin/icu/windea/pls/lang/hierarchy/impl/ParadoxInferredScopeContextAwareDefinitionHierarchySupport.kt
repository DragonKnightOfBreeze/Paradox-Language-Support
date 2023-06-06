package icu.windea.pls.lang.hierarchy.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxInferredScopeContextAwareDefinitionHierarchySupport: ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "inferredScopeContextAware"
        
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }
    
    override val id: String get() = ID
    
    override fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        val definitionType = configExpression.value?.substringBefore('.') ?: return
        if(definitionType !in DEFINITION_TYPES) return
        
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, element.startOffset, definitionInfo.gameType)
        val list = fileData.getOrPut(id) { mutableListOf() } as MutableList
        list.add(info)
    }
}
