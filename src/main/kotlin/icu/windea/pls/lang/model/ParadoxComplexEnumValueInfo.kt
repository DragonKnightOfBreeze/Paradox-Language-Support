package icu.windea.pls.lang.model

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*

data class ParadoxComplexEnumValueInfo(
	val name: String,
	val enumName: String,
	val gameType: ParadoxGameType?
){
	fun getConfig(project: Project): CwtComplexEnumConfig?{
		if(gameType == null) return null
		return getCwtConfig(project).getValue(gameType).complexEnums[enumName]
	}
}