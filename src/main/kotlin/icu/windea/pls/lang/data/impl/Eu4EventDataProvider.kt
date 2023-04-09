package icu.windea.pls.lang.data.impl

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@Suppress("unused")
@WithGameType(ParadoxGameType.Eu4)
class Eu4EventDataProvider : ParadoxDefinitionDataProvider<Eu4EventDataProvider.Data> {
    class Data(data: ParadoxScriptData) : ParadoxDefinitionData {
        val picture: String? by data.get("picture")
    }
    
    override val dataType = Data::class.java
    override val cachedDataKey = Key.create<CachedValue<Data>>("eu4.data.cached.event")
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.gameType == ParadoxGameType.Eu4 && definitionInfo.type == "event"
    }
    
    override fun doGetData(data: ParadoxScriptData) = Data(data)
}