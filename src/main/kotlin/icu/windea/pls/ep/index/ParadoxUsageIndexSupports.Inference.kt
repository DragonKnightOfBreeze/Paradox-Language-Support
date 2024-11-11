package icu.windea.pls.ep.index

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.usageInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxInferredScopeContextAwareDefinitionUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxInferredScopeContextAwareDefinitionUsageInfo> {
    object Constants {
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }

    private val compressComparator = compareBy<ParadoxInferredScopeContextAwareDefinitionUsageInfo> { it.typeExpression }

    override fun id() = ParadoxUsageIndexType.InferredScopeContextAwareDefinition.id

    override fun type() = ParadoxInferredScopeContextAwareDefinitionUsageInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        run {
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if (definitionType !in Constants.DEFINITION_TYPES) return
        }

        val definitionName = element.value
        val typeExpression = config.expression.value ?: return
        val info = ParadoxInferredScopeContextAwareDefinitionUsageInfo(definitionName, typeExpression, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxInferredScopeContextAwareDefinitionUsageInfo>): List<ParadoxInferredScopeContextAwareDefinitionUsageInfo> {
        return value
    }

    override fun writeData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionUsageInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.typeExpression }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionUsageInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionUsageInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readOrReadFrom(previousInfo, { it.typeExpression }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionUsageInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}

class ParadoxEventInOnActionUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxEventInOnActionUsageInfo> {
    private val compressComparator = compareBy<ParadoxEventInOnActionUsageInfo> { it.containingOnActionName }

    override fun id() = ParadoxUsageIndexType.EventInOnAction.id

    override fun type() = ParadoxEventInOnActionUsageInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        run {
            if (definitionInfo.type != "on_action") return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if (definitionType != "event") return
        }

        val eventName = element.value
        val typeExpression = config.expression.value ?: return
        val containingOnActionName = definitionInfo.name
        val info = ParadoxEventInOnActionUsageInfo(eventName, typeExpression, containingOnActionName, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxEventInOnActionUsageInfo>): List<ParadoxEventInOnActionUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxEventInOnActionUsageInfo, previousInfo: ParadoxEventInOnActionUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingOnActionName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionUsageInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionUsageInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readOrReadFrom(previousInfo, { it.containingOnActionName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxEventInOnActionUsageInfo(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
    }
}

class ParadoxEventInEventUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxEventInEventUsageInfo> {
    private val compressComparator = compareBy<ParadoxEventInEventUsageInfo> { it.containingEventName }

    override fun id() = ParadoxUsageIndexType.EventInEvent.id

    override fun type() = ParadoxEventInEventUsageInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        run {
            if (definitionInfo.type != "event") return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if (definitionType != "event") return
        }

        val eventName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxEventInEventUsageInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // xxx_event = { id = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "id" }
            ?.parentConfig
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" } ?: false }
        if (effectConfig == null) return null
        val scopesConfig = effectConfig.configs
            ?.find { it is CwtPropertyConfig && it.key == "scopes" }
        if (scopesConfig == null) return -1
        val scopesElement = element.takeIf { it is ParadoxScriptValue }
            ?.findParentProperty(fromParentBlock = true)
            ?.findProperty("scopes")
        if (scopesElement == null) return -1
        return scopesElement.startOffset
    }

    override fun compressData(value: List<ParadoxEventInEventUsageInfo>): List<ParadoxEventInEventUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxEventInEventUsageInfo, previousInfo: ParadoxEventInEventUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInEventUsageInfo?, gameType: ParadoxGameType): ParadoxEventInEventUsageInfo {
        val eventName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxEventInEventUsageInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}

class ParadoxOnActionInEventUsageIndexSupport : ParadoxUsageIndexSupport<ParadoxOnActionInEventUsageInfo> {
    private val compressComparator = compareBy<ParadoxOnActionInEventUsageInfo> { it.containingEventName }

    override fun id() = ParadoxUsageIndexType.OnActionInEvent.id

    override fun type() = ParadoxOnActionInEventUsageInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        run {
            if (definitionInfo.type != "event") return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if (definitionType != "on_action") return
        }

        val onActionName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxOnActionInEventUsageInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // fire_on_action = { on_action = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "on_action" }
            ?.parentConfig
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" && c.subName == "fire_on_action" } ?: false }
        if (effectConfig == null) return null
        val scopesConfig = effectConfig.configs
            ?.find { it is CwtPropertyConfig && it.key == "scopes" }
        if (scopesConfig == null) return -1
        val scopesElement = element.takeIf { it is ParadoxScriptValue }
            ?.findParentProperty(fromParentBlock = true)
            ?.findProperty("scopes")
        if (scopesElement == null) return -1
        return scopesElement.startOffset
    }

    override fun compressData(value: List<ParadoxOnActionInEventUsageInfo>): List<ParadoxOnActionInEventUsageInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxOnActionInEventUsageInfo, previousInfo: ParadoxOnActionInEventUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.onActionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxOnActionInEventUsageInfo?, gameType: ParadoxGameType): ParadoxOnActionInEventUsageInfo {
        val onActionName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxOnActionInEventUsageInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}
