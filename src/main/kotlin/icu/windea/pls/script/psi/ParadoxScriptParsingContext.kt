package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*

class ParadoxScriptParsingContext(
	val virtualFile: VirtualFile,
	val project: Project?
) {
	val fileInfo = virtualFile.fileInfo
}