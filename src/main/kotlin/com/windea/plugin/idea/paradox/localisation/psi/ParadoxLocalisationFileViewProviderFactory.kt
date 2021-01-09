package com.windea.plugin.idea.paradox.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*

class ParadoxLocalisationFileViewProviderFactory: FileViewProviderFactory {
	override fun createFileViewProvider(file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean): FileViewProvider {
		return ParadoxLocalisationFileViewProvider(manager, file, eventSystemEnabled)
	}
}