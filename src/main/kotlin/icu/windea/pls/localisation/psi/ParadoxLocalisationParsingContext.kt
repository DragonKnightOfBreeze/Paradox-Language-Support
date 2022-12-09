package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*

class ParadoxLocalisationParsingContext(
	val virtualFile: VirtualFile,
	val project: Project?
) {
	val fileInfo = virtualFile.fileInfo
	
	var currentKey: String? = null
	
	val gameType get() = fileInfo?.rootInfo?.gameType
	
	@WithGameType(ParadoxGameType.Stellaris)
	val stellarisNameFormatKeys =
		project?.let { project -> StellarisNameFormatHandler.getAllKeys(project) }
}