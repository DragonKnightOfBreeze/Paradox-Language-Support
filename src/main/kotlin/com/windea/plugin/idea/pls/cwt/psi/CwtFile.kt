package com.windea.plugin.idea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.cwt.*
import com.windea.plugin.idea.pls.cwt.CwtLanguage

class CwtFile(
  viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
	override fun getFileType() = CwtFileType
}