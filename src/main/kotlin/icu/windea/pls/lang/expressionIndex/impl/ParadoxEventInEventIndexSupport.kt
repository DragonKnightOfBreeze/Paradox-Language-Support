package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

private val compressComparator = compareBy<ParadoxEventInEventInfo> { it.containingEventName }

class ParadoxEventInEventIndexSupport : ParadoxExpressionIndexSupport<ParadoxEventInEventInfo> {
    override fun id() = ParadoxExpressionIndexId.EventInEvent.id
    
    override fun type() = ParadoxEventInEventInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            if(definitionInfo.type != "event") return
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataType.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType != "event") return
        }
        
        val eventName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventHandler.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxEventInEventInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // xxx_event = { id = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "id" }
            ?.parentConfig
            ?.takeIf { it.inlineableConfig?.castOrNull<CwtAliasConfig>()?.let { c -> c.name == "effect" } ?: false }
        if(effectConfig == null) return null
        val scopesConfig = effectConfig.configs
            ?.find { it is CwtPropertyConfig && it.key == "scopes" }
        if(scopesConfig == null) return -1
        val scopesElement = element.takeIf { it is ParadoxScriptValue }
            ?.findParentProperty(fromParentBlock = true)
            ?.findProperty("scopes")
        if(scopesElement == null) return -1
        return scopesElement.startOffset
    }
    
    override fun compressData(value: List<ParadoxEventInEventInfo>): List<ParadoxEventInEventInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxEventInEventInfo, previousInfo: ParadoxEventInEventInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxEventInEventInfo?, gameType: ParadoxGameType): ParadoxEventInEventInfo {
        val eventName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxEventInEventInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}