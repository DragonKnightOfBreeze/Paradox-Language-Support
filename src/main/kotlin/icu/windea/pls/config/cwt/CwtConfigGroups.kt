package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.core.model.*

interface CwtConfigGroups {
	val project: Project
	val groups: Map<String, CwtConfigGroup>
	
	val core get() = getValue("core")
	
	val ck2 get() = getValue(ParadoxGameType.Ck2)
	val ck3 get() = getValue(ParadoxGameType.Ck3)
	val eu4 get() = getValue(ParadoxGameType.Eu4)
	val hoi4 get() = getValue(ParadoxGameType.Hoi4)
	val ir get() = getValue(ParadoxGameType.Ir)
	val stellaris get() = getValue(ParadoxGameType.Stellaris)
	val vic2 get() = getValue(ParadoxGameType.Vic2)
	val vic3 get() = getValue(ParadoxGameType.Vic3)
	
	operator fun get(key: String) = groups.get(key)
	
	fun getValue(key: String) = groups.getValue(key)
	
	operator fun get(key: ParadoxGameType) = groups.get(key.id)
	
	fun getValue(key: ParadoxGameType) = groups.getValue(key.id)
}

