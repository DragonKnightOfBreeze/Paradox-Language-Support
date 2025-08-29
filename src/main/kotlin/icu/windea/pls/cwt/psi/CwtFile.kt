package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.navigation.CwtItemPresentation

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
