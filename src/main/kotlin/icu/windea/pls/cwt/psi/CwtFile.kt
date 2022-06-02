package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*

class CwtFile(
  viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
	override fun getFileType() = CwtFileType
	
	val block get() = findOptionalChild<CwtRootBlock>()
	
	val propertyList get() =  block?.propertyList.orEmpty()
	
	val valueList get() = block?.valueList.orEmpty()
}