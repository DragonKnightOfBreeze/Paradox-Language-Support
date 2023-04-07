package icu.windea.pls.lang.data

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

class StellarisEventDataProvider : ParadoxDefinitionDataProvider<StellarisEventDataProvider.Data> {
    class Data(data: ParadoxScriptData) : ParadoxDefinitionData{
        val picture: String? by data.get("picture")
        val hide_window : Boolean by data.get("hide_window", false)
        val is_triggered_only: Boolean by data.get("is_triggered_only", false)
        val major: Boolean by data.get("major", false)
        val diplomatic: Boolean by data.get("diplomatic", false)
    }
    
    override val dataType = Data::class.java
    override val cachedDataKey = Key.create<CachedValue<Data>>("paradox.data.cached.event")
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.gameType == ParadoxGameType.Stellaris && definitionInfo.type == "event"
    }
    
    override fun doGetData(data: ParadoxScriptData) = Data(data)
}
