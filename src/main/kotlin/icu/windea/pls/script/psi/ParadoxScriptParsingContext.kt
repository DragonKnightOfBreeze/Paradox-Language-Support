package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import icu.windea.pls.core.model.*

class ParadoxScriptParsingContext(
	val fileInfo: ParadoxFileInfo,
	val project: Project?
)