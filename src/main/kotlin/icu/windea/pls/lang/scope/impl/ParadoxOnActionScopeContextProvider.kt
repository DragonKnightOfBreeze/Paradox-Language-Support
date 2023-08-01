package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.progress.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*

class ParadoxOnActionScopeContextProvider : ParadoxDefinitionScopeContextProvider {
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        ProgressManager.checkCanceled()
        if(definitionInfo.type != "on_action") return null
        //直接使用来自game_rules.cwt的作用域信息
        val configGroup = definitionInfo.configGroup
        val config = configGroup.onActions.getByTemplate(definitionInfo.name, definition, configGroup)
        val result = config?.config?.scopeContext
        return result
    }
}