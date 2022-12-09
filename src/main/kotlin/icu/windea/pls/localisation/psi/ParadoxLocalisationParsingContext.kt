package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*

class ParadoxLocalisationParsingContext(
	val fileInfo: ParadoxFileInfo,
	val project: Project?
) {
	var currentKey: String? = null
	
	val gameType get() = fileInfo.rootInfo.gameType
	
	//@WithGameType(ParadoxGameType.Stellaris)
	//val stellarisNameFormatKeys =
	//	project?.let { project -> StellarisNameFormatHandler.getAllKeys(project) }
	
	fun isStellarisNameFormatKey() : Boolean{
		if(gameType != ParadoxGameType.Stellaris) return false
		if(!fileInfo.path.canBeLocalisationPath()) return false
		return currentKey?.startsWith(StellarisNameFormatHandler.prefix) == true
	}
}