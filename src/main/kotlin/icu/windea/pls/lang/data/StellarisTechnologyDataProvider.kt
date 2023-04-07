package icu.windea.pls.lang.data

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyDataProvider : ParadoxDefinitionDataProvider<StellarisTechnologyDataProvider.Data> {
    class Data(data: ParadoxScriptData): ParadoxDefinitionData {
        val icon: String? by data.get("icon")
        val tier: String? by data.get("tier")
        val area: String? by data.get("area")
        val category: Set<String>? by data.get("category")
    
        val cost: Int? by data.get("cost")
        //val cost_per_level: Int? by data.get("cost_per_level")
        val levels: Int? by data.get("levels")
        
        val start_tech: Boolean by data.get("start_tech", false)
        val is_rare: Boolean by data.get("is_rare", false)
        val is_dangerous: Boolean by data.get("is_dangerous", false)
        val is_insight: Boolean by data.get("is_insight", false)
        
        val gateway: String? by data.get("gateway")
        val prerequisites: Set<String> by data.get("prerequisites", emptySet()) 
    }
    
    override val dataType = Data::class.java
    override val cachedDataKey = Key.create<CachedValue<Data>>("stellaris.data.cached.technology")
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.gameType == ParadoxGameType.Stellaris && definitionInfo.type == "technology"
    }
    
    override fun doGetData(data: ParadoxScriptData) = Data(data)
}
