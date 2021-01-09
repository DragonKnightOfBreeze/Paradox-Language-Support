package com.windea.plugin.idea.paradox.script.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*

class ParadoxScriptFileViewProviderFactory: FileViewProviderFactory {
	override fun createFileViewProvider(file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean): FileViewProvider {
		return ParadoxScriptFileViewProvider(manager, file, eventSystemEnabled)
	}
}