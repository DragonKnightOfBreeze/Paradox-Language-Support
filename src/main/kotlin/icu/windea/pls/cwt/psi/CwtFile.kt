package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*

class CwtFile(
  viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
	override fun getFileType() = CwtFileType
	
	val block get() = findChildByClass(CwtRootBlock::class.java)
	
	val properties get() =  block?.propertyList.orEmpty()
	
	val values get() = block?.valueList.orEmpty()
}