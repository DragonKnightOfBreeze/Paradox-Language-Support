package icu.windea.pls.ep.index

import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.core.orNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.ofKey
import icu.windea.pls.lang.select.one
import icu.windea.pls.lang.select.parentOfKey
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.index.ParadoxEventInEventIndexInfo
import icu.windea.pls.model.index.ParadoxEventInOnActionIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxInferredScopeContextAwareDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxOnActionInEventIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.io.DataInput
import java.io.DataOutput

class ParadoxInferredScopeContextAwareDefinitionMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxInferredScopeContextAwareDefinitionIndexInfo> {
    private val compressComparator = compareBy<ParadoxInferredScopeContextAwareDefinitionIndexInfo> { it.typeExpression }

    override val indexInfoType = ParadoxIndexInfoTypes.InferredScopeContextAwareDefinition

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val definitionName = element.value
        val typeExpression = config.configExpression.value ?: return
        val gameType = config.configGroup.gameType
        val info = ParadoxInferredScopeContextAwareDefinitionIndexInfo(definitionName, typeExpression, element.startOffset, gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesInferredScopeContextAwareDefinitionReference(config.configExpression, config.configGroup)
    }

    override fun compressData(value: List<ParadoxInferredScopeContextAwareDefinitionIndexInfo>): List<ParadoxInferredScopeContextAwareDefinitionIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun saveData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionIndexInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.typeExpression }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.definitionElementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionIndexInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionIndexInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readOrReadFrom(previousInfo, { it.typeExpression }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionIndexInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}

class ParadoxEventInOnActionMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxEventInOnActionIndexInfo> {
    private val compressComparator = compareBy<ParadoxEventInOnActionIndexInfo> { it.containingOnActionName }

    override val indexInfoType = ParadoxIndexInfoTypes.EventInOnAction

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        if (info !is ParadoxDefinitionInfo) return
        if (info.type != ParadoxDefinitionTypes.onAction) return

        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val eventName = element.value
        val typeExpression = config.configExpression.value ?: return
        val containingOnActionName = info.name
        val info = ParadoxEventInOnActionIndexInfo(eventName, typeExpression, containingOnActionName, info.gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesEventReference(config.configExpression)
    }

    override fun compressData(value: List<ParadoxEventInOnActionIndexInfo>): List<ParadoxEventInOnActionIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxEventInOnActionIndexInfo, previousInfo: ParadoxEventInOnActionIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingOnActionName }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionIndexInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionIndexInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readOrReadFrom(previousInfo, { it.containingOnActionName }, { storage.readUTFFast() })
        return ParadoxEventInOnActionIndexInfo(eventName, typeExpression, containingOnActionName, gameType)
    }
}

class ParadoxEventInEventMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxEventInEventIndexInfo> {
    private val compressComparator = compareBy<ParadoxEventInEventIndexInfo> { it.containingEventName }

    override val indexInfoType = ParadoxIndexInfoTypes.EventInEvent

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        if (info !is ParadoxDefinitionInfo) return
        if (info.type != ParadoxDefinitionTypes.event) return

        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val eventName = element.value
        val containingEventName = info.name
        val containingEventScope = ParadoxEventManager.getScope(info)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxEventInEventIndexInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, info.gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesEventReference(config.configExpression)
    }

    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // xxx_event = { id = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "id" }
            ?.parentConfig
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" } ?: false }
        if (effectConfig == null) return null
        if (element !is ParadoxScriptString) return -1
        val scopesConfig = effectConfig.configs?.find { it is CwtPropertyConfig && it.key == "scopes" }
        if (scopesConfig == null) return -1
        val scopesElement = selectScope { element.parentOfKey(fromBlock = true)?.properties()?.ofKey("scopes")?.one() }
        if (scopesElement == null) return -1
        if (scopesElement.block == null) return -1 // extra check
        return scopesElement.startOffset
    }

    override fun compressData(value: List<ParadoxEventInEventIndexInfo>): List<ParadoxEventInEventIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun saveData(storage: DataOutput, info: ParadoxEventInEventIndexInfo, previousInfo: ParadoxEventInEventIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxEventInEventIndexInfo?, gameType: ParadoxGameType): ParadoxEventInEventIndexInfo {
        val eventName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        return ParadoxEventInEventIndexInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, gameType)
    }
}

class ParadoxOnActionInEventMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxOnActionInEventIndexInfo> {
    private val compressComparator = compareBy<ParadoxOnActionInEventIndexInfo> { it.containingEventName }

    override val indexInfoType = ParadoxIndexInfoTypes.OnActionInEvent

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        if (info !is ParadoxDefinitionInfo) return
        if (info.type != ParadoxDefinitionTypes.event) return

        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val onActionName = element.value
        val containingEventName = info.name
        val containingEventScope = ParadoxEventManager.getScope(info)
        val scopesElementOffset = getScopesElementOffset(element, config) ?: return
        val info = ParadoxOnActionInEventIndexInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, info.gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesOnActionReference(config.configExpression)
    }

    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // fire_on_action = { on_action = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "on_action" }
            ?.parentConfig
            ?.takeIf { it is CwtPropertyConfig && it.aliasConfig?.let { c -> c.name == "effect" && c.subName == "fire_on_action" } ?: false }
        if (effectConfig == null) return null
        if (element !is ParadoxScriptString) return -1
        val scopesConfig = effectConfig.configs?.find { it is CwtPropertyConfig && it.key == "scopes" }
        if (scopesConfig == null) return -1
        val scopesElement = selectScope { element.parentOfKey(fromBlock = true)?.properties()?.ofKey("scopes")?.one() }
        if (scopesElement == null) return -1
        if (scopesElement.block == null) return -1 // extra check
        return scopesElement.startOffset
    }

    override fun compressData(value: List<ParadoxOnActionInEventIndexInfo>): List<ParadoxOnActionInEventIndexInfo> {
        return value.sortedWith(compressComparator)
    }

    override fun saveData(storage: DataOutput, info: ParadoxOnActionInEventIndexInfo, previousInfo: ParadoxOnActionInEventIndexInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.onActionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingEventName }, { storage.writeUTFFast(it) })
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxOnActionInEventIndexInfo?, gameType: ParadoxGameType): ParadoxOnActionInEventIndexInfo {
        val onActionName = storage.readUTFFast()
        val containingEventName = storage.readOrReadFrom(previousInfo, { it.containingEventName }, { storage.readUTFFast() })
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        return ParadoxOnActionInEventIndexInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, gameType)
    }
}
