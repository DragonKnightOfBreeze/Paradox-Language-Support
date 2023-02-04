package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

class ParadoxLocalisationParsingContext(
	val project: Project?,
	val fileInfo: ParadoxFileInfo?
) {
	val gameType get() = fileInfo?.rootInfo?.gameType
	
	var currentKey: String? = null
}