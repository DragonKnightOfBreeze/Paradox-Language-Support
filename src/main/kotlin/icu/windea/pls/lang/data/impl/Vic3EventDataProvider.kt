package icu.windea.pls.lang.data.impl

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@Suppress("unused")
@WithGameType(ParadoxGameType.Vic3)
class Vic3EventDataProvider : ParadoxDefinitionDataProvider<Vic3EventDataProvider.Data> {
    class Data(data: ParadoxScriptData) : ParadoxDefinitionData {
        val icon: String? by data.get("icon")
        val left_icon: String? by data.get("left_icon")
        val right_icon: String? by data.get("right_icon")
        val minor_left_icon: String? by data.get("minor_left_icon")
        val minor_right_icon: String? by data.get("minor_right_icon")
        val gui_window: String? by data.get("gui_window")
    }
    
    override val dataType = Data::class.java
    override val cachedDataKey = Key.create<CachedValue<Data>>("vic3.data.cached.event")
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.gameType == ParadoxGameType.Vic3 && definitionInfo.type == "event"
    }
    
    override fun doGetData(data: ParadoxScriptData) = Data(data)
}