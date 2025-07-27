package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.navigation.*

class CwtFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = IFileElementType("CWT_FILE", CwtLanguage)
    }

    val block get() = findChild<CwtRootBlock>()

    val propertyList get() = block?.propertyList.orEmpty()

    val valueList get() = block?.valueList.orEmpty()

    override fun getFileType() = CwtFileType

    override fun getPresentation() = CwtItemPresentation(this)
}
