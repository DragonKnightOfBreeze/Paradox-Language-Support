package icu.windea.pls.lang.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

interface CwtConfigGroups {
    val project: Project
    val groups: Map<String, CwtConfigGroup>
    
    fun get(key: String) = groups.getValue(key)
    
    fun get(key: ParadoxGameType?) = groups.getValue(key?.id ?: "core")
    
    val core get() = get("core")
    
    val stellaris get() = get(ParadoxGameType.Stellaris)
    val ck2 get() = get(ParadoxGameType.Ck2)
    val ck3 get() = get(ParadoxGameType.Ck3)
    val eu4 get() = get(ParadoxGameType.Eu4)
    val hoi4 get() = get(ParadoxGameType.Hoi4)
    val ir get() = get(ParadoxGameType.Ir)
    val vic2 get() = get(ParadoxGameType.Vic2)
    val vic3 get() = get(ParadoxGameType.Vic3)
}

