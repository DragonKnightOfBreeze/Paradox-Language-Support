package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.structureView.*
import icu.windea.pls.localisation.structureView.*

class CwtFile(
  viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
	override fun getFileType() = CwtFileType
	
	override fun getPresentation(): ItemPresentation {
		return CwtFileTreeElement(this)
	}
	
	val block get() = findChildByClass(CwtRootBlock::class.java)
	
	val properties get() =  block?.propertyList.orEmpty()
	
	val values get() = block?.valueList.orEmpty()
}