package icu.windea.pls.lang.hierarchy.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxEventInEventDefinitionHierarchySupport : ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "event.in.event"
        
        //val containingEventNameKey = Key.create<String>("paradox.definition.hierarchy.event.in.event.containingEventName") //definitionName
        val containingEventScopeKey = Key.create<String>("paradox.definition.hierarchy.event.in.event.containingEventScope")
    }
    
    override val id: String get() = ID
    
    override fun indexData(fileData: MutableMap<String, MutableList<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        if(definitionInfo.type != "event") return
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        val definitionType = configExpression.value?.substringBefore('.') ?: return
        if(definitionType != "event") return
        
        //elementOffset is unused yet by this support
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, -1 /*element.startOffset*/, definitionInfo.gameType)
        info.putUserData(containingEventScopeKey, ParadoxEventHandler.getScope(definitionInfo).orEmpty())
        fileData.getOrPut(id) { mutableListOf() }.add(info)
        //TODO 需要带上可能的作用域映射信息
    }
    
    override fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {
        storage.writeString(data.getUserData(containingEventScopeKey).orEmpty())
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
        storage.readString().takeIfNotEmpty()?.let { data.putUserData(containingEventScopeKey, it) }
    }
}