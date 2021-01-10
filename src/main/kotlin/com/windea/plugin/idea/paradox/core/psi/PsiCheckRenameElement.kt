package com.windea.plugin.idea.paradox.core.psi

import com.intellij.psi.*

interface PsiCheckRenameElement:PsiNamedElement {
	fun checkRename():Boolean{
		return true
	}
}