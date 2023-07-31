package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxEventInEventDefinitionHierarchyIndex.Info>>("paradox.eventInEvent.definitionHierarchy.index")
private const val VERSION = 32 //1.1.5

class ParadoxEventInEventDefinitionHierarchyIndex : ParadoxDefinitionHierarchyIndex<ParadoxEventInEventDefinitionHierarchyIndex.Info>() {
    data class Info(
        val eventName: String,
        val containingEventName: String,
        val containingEventScope: String?,
        val scopesElementOffset: Int,
        override val elementOffset: Int,
        override val gameType: ParadoxGameType
    ) : ParadoxExpressionInfo {
        @Volatile override var virtualFile: VirtualFile? = null
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<Info>>) {
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
        val info = Info(eventName, containingEventName, containingEventScope, scopesElementOffset, element.startOffset, definitionInfo.gameType)
        val list = fileData.getOrPut("") { mutableListOf() } as MutableList
        list.add(info)
    }
    
    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // xxx_event = { id = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "id" }
            ?.parent
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
    
    override fun writeData(storage: DataOutput, value: List<Info>) {
        val size = value.size
        storage.writeIntFast(size)
        if(size == 0) return
        val firstInfo = value.first()
        storage.writeByte(firstInfo.gameType.toByte())
        value.forEachFast { info ->
            storage.writeUTFFast(info.eventName)
            storage.writeUTFFast(info.containingEventName)
            storage.writeUTFFast(info.containingEventScope.orEmpty())
            storage.writeIntFast(info.scopesElementOffset)
            storage.writeIntFast(info.elementOffset)
        }
    }
    
    override fun readData(storage: DataInput): List<Info> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val gameType = storage.readByte().toGameType()
        val result = mutableListOf<Info>()
        repeat(size) {
            val eventName = storage.readUTFFast()
            val containingEventName = storage.readUTFFast()
            val containingEventScope = storage.readUTFFast().takeIfNotEmpty()
            val scopesElementOffset = storage.readIntFast()
            val elementOffset = storage.readIntFast()
            val info = Info(eventName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
            result += info
        }
        return result
    }
}