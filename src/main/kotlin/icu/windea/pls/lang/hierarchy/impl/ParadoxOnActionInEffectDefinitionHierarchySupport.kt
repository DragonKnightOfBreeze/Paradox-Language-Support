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

class ParadoxOnActionInEffectDefinitionHierarchySupport : ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "onAction.in.effect"
        
        val containingEventScopeKey = Key.create<String>("paradox.definition.hierarchy.onAction.in.effect.containingEventScope")
        val scopesElementOffsetKey = Key.create<Int>("paradox.definition.hierarchy.onAction.in.effect.scopesElementOffset")
    }
    
    override val id: String get() = ID
    
    override fun indexData(fileData: MutableList<ParadoxDefinitionHierarchyInfo>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return
        val definitionType = configExpression.value?.substringBefore('.') ?: return
        if(definitionType != "on_action") return
        
        val containingEventScope = if(definitionInfo.type == "event") ParadoxEventHandler.getScope(definitionInfo) else null
        val scopesElementOffset = getScopesElementOffset(element, config)
        if(scopesElementOffset == null) return
        
        //elementOffset has not been used yet by this support
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, -1 /*element.startOffset*/, definitionInfo.gameType)
        containingEventScope?.takeIfNotEmpty()?.let { info.putUserData(containingEventScopeKey, it) }
        scopesElementOffset.let { info.putUserData(scopesElementOffsetKey, it) }
        fileData.add(info)
    }
    
    private fun getScopesElementOffset(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>): Int? {
        // fire_on_action = { on_action = <id> scopes = { ... } }
        val effectConfig = config.takeIf { it is CwtValueConfig }
            ?.memberConfig
            ?.takeIf { it is CwtPropertyConfig && it.key == "on_action" }
            ?.parent
            ?.takeIf { it.inlineableConfig?.castOrNull<CwtAliasConfig>()?.let { c -> c.name == "effect" && c.subName == "fire_on_action" } ?: false }
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
        storage.writeString(data.getUserData(containingEventScopeKey).orEmpty())
        storage.writeInt(data.getUserData(scopesElementOffsetKey) ?: -1)
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
        storage.readString().takeIfNotEmpty()?.let { data.putUserData(containingEventScopeKey, it) }
        storage.readInt().let { data.putUserData(scopesElementOffsetKey, it) }
    }
}