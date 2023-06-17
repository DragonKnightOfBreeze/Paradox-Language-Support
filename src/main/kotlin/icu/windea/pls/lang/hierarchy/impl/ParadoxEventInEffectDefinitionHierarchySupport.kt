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

class ParadoxEventInEffectDefinitionHierarchySupport : ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "event.in.effect"
        
        val containingEventScopeKey = Key.create<String>("paradox.definition.hierarchy.event.in.effect.containingEventScope")
        val scopesElementOffsetKey = Key.create<Int>("paradox.definition.hierarchy.event.in.effect.scopesElementOffset")
    }
    
    override val id: String get() = ID
    
    override fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        val definitionType = configExpression.value?.substringBefore('.') ?: return
        if(definitionType != "event") return
        
        val containingEventScope = if(definitionInfo.type == "event") ParadoxEventHandler.getScope(definitionInfo) else null
        val scopesElementOffset = getScopesElementOffset(element, config)
        if(scopesElementOffset == null) return
        
        //elementOffset has not been used yet by this support
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config, definitionInfo, -1 /*element.startOffset*/)
        containingEventScope?.takeIfNotEmpty()?.let { info.putUserData(containingEventScopeKey, it) }
        scopesElementOffset.let { info.putUserData(scopesElementOffsetKey, it) }
        val list = fileData.getOrPut(id) { mutableListOf() } as MutableList
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
    
    override fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {
        storage.writeUTFFast(data.getUserData(containingEventScopeKey).orEmpty())
        storage.writeIntFast(data.getUserData(scopesElementOffsetKey) ?: -1)
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
        storage.readUTFFast().takeIfNotEmpty()?.let { data.putUserData(containingEventScopeKey, it) }
        storage.readIntFast().let { data.putUserData(scopesElementOffsetKey, it) }
    }
}
