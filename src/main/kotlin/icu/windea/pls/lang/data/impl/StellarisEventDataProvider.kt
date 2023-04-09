package icu.windea.pls.lang.data.impl

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@Suppress("unused")
@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventDataProvider : ParadoxDefinitionDataProvider<StellarisEventDataProvider.Data> {
    class Data(data: ParadoxScriptData) : ParadoxDefinitionData {
        val picture: String? by data.get("picture")
    }
    
    override val dataType = Data::class.java
    override val cachedDataKey = Key.create<CachedValue<Data>>("stellaris.data.cached.event")
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.gameType == ParadoxGameType.Stellaris && definitionInfo.type == "event"
    }
    
    override fun doGetData(data: ParadoxScriptData) = Data(data)
}
