package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.testFramework.*
import icu.windea.pls.*

class ParadoxLocalisationFileViewProvider(
	manager: PsiManager,
	virtualFile: VirtualFile,
	eventSystemEnabled: Boolean
): SingleRootFileViewProvider(manager,virtualFile,eventSystemEnabled) {
	//virtualFile可能是LightVirtualFile

	
	override fun getPsiInner(target: Language): PsiFile? {
		val psi =  super.getPsiInner(target)
		//传递fileInfo，注意virtualFile可能是LightVirtualFile
		val fileInfo  = when(val file = virtualFile) {
			is LightVirtualFile -> file.originalFile?.fileInfo
			else -> file.fileInfo
		}
		psi?.putUserData(paradoxFileInfoKey,fileInfo)
		return psi
	}
}

