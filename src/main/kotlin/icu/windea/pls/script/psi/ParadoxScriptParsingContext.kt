package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import icu.windea.pls.core.model.*

class ParadoxScriptParsingContext(
	val project: Project?,
	val fileInfo: ParadoxFileInfo?
) {
	val gameType get() = fileInfo?.rootInfo?.gameType
}