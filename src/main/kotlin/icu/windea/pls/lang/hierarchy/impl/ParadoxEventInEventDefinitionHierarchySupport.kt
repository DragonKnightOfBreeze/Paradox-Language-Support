package icu.windea.pls.lang.hierarchy.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxEventInEventDefinitionHierarchySupport: ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "event.in.event"
    }
    
    override val id: String = ID
    
    override fun indexData(fileData: MutableMap<String, MutableList<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        if(definitionInfo.type != "event") return
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        if(configExpression.value?.substringBefore('.') != "event") return
        
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, element.startOffset, definitionInfo.gameType)
        fileData.getOrPut(id) { mutableListOf() }.add(info)
        //TODO 需要带上可能的作用域映射信息
    }
    
    override fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {
        super.saveData(storage, data)
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
        super.readData(storage, data)
    }
}