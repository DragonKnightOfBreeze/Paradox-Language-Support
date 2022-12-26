package icu.windea.pls.core.model

import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*

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