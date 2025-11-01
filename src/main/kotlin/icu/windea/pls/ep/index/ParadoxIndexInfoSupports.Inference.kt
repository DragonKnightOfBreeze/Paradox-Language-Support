package icu.windea.pls.ep.index

import com.intellij.psi.util.startOffset
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.orNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.index.ParadoxEventInEventIndexInfo
import icu.windea.pls.model.index.ParadoxEventInOnActionIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxInferredScopeContextAwareDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxOnActionInEventIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentProperty
import icu.windea.pls.script.psi.findProperty
import java.io.DataInput
import java.io.DataOutput

class ParadoxInferredScopeContextAwareDefinitionIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxInferredScopeContextAwareDefinitionIndexInfo> {
    object Constants {
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }

    private val compressComparator = compareBy<ParadoxInferredScopeContextAwareDefinitionIndexInfo> { it.typeExpression }

    override val id = ParadoxIndexInfoType.InferredScopeContextAwareDefinition.id

    override val type = ParadoxInferredScopeContextAwareDefinitionIndexInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        run {
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
            val dataType = config.configExpression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.configExpression.value?.substringBefore('.') ?: return
            if (definitionType !in Constants.DEFINITION_TYPES) return
        }

        val definitionName = element.value
        val typeExpression = config.configExpression.value ?: return
        val info = ParadoxInferredScopeContextAwareDefinitionIndexInfo(definitionName, typeExpression, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxInferredScopeContextAwareDefinitionIndexInfo>): List<ParadoxInferredScopeContextAwareDefinitionIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionIndexInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.typeExpression }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionIndexInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionIndexInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readOrReadFrom(previousInfo, { it.typeExpression }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionIndexInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}

class ParadoxEventInOnActionIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxEventInOnActionIndexInfo> {
    private val compressComparator = compareBy<ParadoxEventInOnActionIndexInfo> { it.containingOnActionName }

    override val id = ParadoxIndexInfoType.EventInOnAction.id

    override val type = ParadoxEventInOnActionIndexInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        run {
            if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
            val dataType = config.configExpression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.configExpression.value?.substringBefore('.') ?: return
            if (definitionType != ParadoxDefinitionTypes.Event) return
        }

        val eventName = element.value
        val typeExpression = config.configExpression.value ?: return
        val containingOnActionName = definitionInfo.name
        val info = ParadoxEventInOnActionIndexInfo(eventName, typeExpression, containingOnActionName, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxEventInOnActionIndexInfo>): List<ParadoxEventInOnActionIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxEventInOnActionIndexInfo, previousInfo: ParadoxEventInOnActionIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingOnActionName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionIndexInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionIndexInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readOrReadFrom(previousInfo, { it.containingOnActionName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxEventInOnActionIndexInfo(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
    }
}

class ParadoxEventInEventIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxEventInEventIndexInfo> {
    private val compressComparator = compareBy<ParadoxEventInEventIndexInfo> { it.containingEventName }

    override val id = ParadoxIndexInfoType.EventInEvent.id

    override val type = ParadoxEventInEventIndexInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        run {
            if (definitionInfo.type != ParadoxDefinitionTypes.Event) return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
            val dataType = config.configExpression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.configExpression.value?.substringBefore('.') ?: return
            if (definitionType != ParadoxDefinitionTypes.Event) return
        }

        val eventName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxEventInEventIndexInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
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

    override fun compressData(value: List<ParadoxEventInEventIndexInfo>): List<ParadoxEventInEventIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxEventInEventIndexInfo, previousInfo: ParadoxEventInEventIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInEventIndexInfo?, gameType: ParadoxGameType): ParadoxEventInEventIndexInfo {
        val eventName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxEventInEventIndexInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}

class ParadoxOnActionInEventIndexInfoSupport : ParadoxIndexInfoSupport<ParadoxOnActionInEventIndexInfo> {
    private val compressComparator = compareBy<ParadoxOnActionInEventIndexInfo> { it.containingEventName }

    override val id = ParadoxIndexInfoType.OnActionInEvent.id

    override val type = ParadoxOnActionInEventIndexInfo::class.java

    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        run {
            if (definitionInfo.type != ParadoxDefinitionTypes.Event) return
            val expression = element.value
            if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
            val dataType = config.configExpression.type
            if (dataType != CwtDataTypes.Definition) return
            val definitionType = config.configExpression.value?.substringBefore('.') ?: return
            if (definitionType != ParadoxDefinitionTypes.OnAction) return
        }

        val onActionName = element.value
        val containingEventName = definitionInfo.name
        val containingEventScope = ParadoxEventManager.getScope(definitionInfo)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxOnActionInEventIndexInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
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

    override fun compressData(value: List<ParadoxOnActionInEventIndexInfo>): List<ParadoxOnActionInEventIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun writeData(storage: DataOutput, info: ParadoxOnActionInEventIndexInfo, previousInfo: ParadoxOnActionInEventIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.onActionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxOnActionInEventIndexInfo?, gameType: ParadoxGameType): ParadoxOnActionInEventIndexInfo {
        val onActionName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxOnActionInEventIndexInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}
