package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*

class ParadoxLocalisationParsingContext(
	val fileInfo: ParadoxFileInfo?,
	val project: Project
) {
	var currentKey: String? = null
	
	val gameType get() = fileInfo?.rootInfo?.gameType
	
	@WithGameType(ParadoxGameType.Stellaris)
	val stellarisNameFormatKeys = StellarisNameFormatHandler.getAllKeys(project)
}