package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*

class CwtFile(
  viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
	override fun getFileType() = CwtFileType
	
	val rootBlock get() = findChildByClass(CwtRootBlock::class.java)
	
	val properties get() =  rootBlock?.propertyList.orEmpty()
	
	val values get() = rootBlock?.valueList.orEmpty()
}