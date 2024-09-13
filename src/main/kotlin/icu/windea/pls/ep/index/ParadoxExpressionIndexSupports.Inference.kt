package icu.windea.pls.ep.index

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxInferredScopeContextAwareDefinitionIndexSupport : ParadoxExpressionIndexSupport<ParadoxInferredScopeContextAwareDefinitionInfo> {
    object Constants {
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }
    
    private val compressComparator = compareBy<ParadoxInferredScopeContextAwareDefinitionInfo> { it.typeExpression }
    
    override fun id() = ParadoxExpressionIndexId.InferredScopeContextAwareDefinition.code
    
    override fun type() = ParadoxInferredScopeContextAwareDefinitionInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType !in Constants.DEFINITION_TYPES) return
        }
        
        val definitionName = element.value
        val typeExpression = config.expression.value ?: return
        val info = ParadoxInferredScopeContextAwareDefinitionInfo(definitionName, typeExpression, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxInferredScopeContextAwareDefinitionInfo>): List<ParadoxInferredScopeContextAwareDefinitionInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.typeExpression }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readOrReadFrom(previousInfo, { it.typeExpression }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}

class ParadoxEventInOnActionIndexSupport : ParadoxExpressionIndexSupport<ParadoxEventInOnActionInfo> {
    private val compressComparator = compareBy<ParadoxEventInOnActionInfo> { it.containingOnActionName }
    
    override fun id() = ParadoxExpressionIndexId.EventInOnAction.code
    
    override fun type() = ParadoxEventInOnActionInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            if(definitionInfo.type != "on_action") return
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType != "event") return
        }
        
        val eventName = element.value
        val typeExpression = config.expression.value ?: return
        val containingOnActionName = definitionInfo.name
        val info = ParadoxEventInOnActionInfo(eventName, typeExpression, containingOnActionName, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxEventInOnActionInfo>): List<ParadoxEventInOnActionInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxEventInOnActionInfo, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingOnActionName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readOrReadFrom(previousInfo, { it.containingOnActionName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxEventInOnActionInfo(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
    }
}

class ParadoxEventInEventIndexSupport : ParadoxExpressionIndexSupport<ParadoxEventInEventInfo> {
    private val compressComparator = compareBy<ParadoxEventInEventInfo> { it.containingEventName }
    
    override fun id() = ParadoxExpressionIndexId.EventInEvent.code
    
    override fun type() = ParadoxEventInEventInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            if(definitionInfo.type != "event") return
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType != "event") return
        }
        
        val eventName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
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
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" } ?: false }
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

class ParadoxOnActionInEventIndexSupport : ParadoxExpressionIndexSupport<ParadoxOnActionInEventInfo> {
    private val compressComparator = compareBy<ParadoxOnActionInEventInfo> { it.containingEventName }
    
    override fun id() = ParadoxExpressionIndexId.OnActionInEvent.code
    
    override fun type() = ParadoxOnActionInEventInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            if(definitionInfo.type != "event") return
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType != "on_action") return
        }
        
        val onActionName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxOnActionInEventInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // fire_on_action = { on_action = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "on_action" }
            ?.parentConfig
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" && c.subName == "fire_on_action" } ?: false }
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
    
    override fun compressData(value: List<ParadoxOnActionInEventInfo>): List<ParadoxOnActionInEventInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxOnActionInEventInfo, previousInfo: ParadoxOnActionInEventInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.onActionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxOnActionInEventInfo?, gameType: ParadoxGameType): ParadoxOnActionInEventInfo {
        val onActionName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxOnActionInEventInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}
