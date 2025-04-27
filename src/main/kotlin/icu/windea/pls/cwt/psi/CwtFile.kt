package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*

class CwtFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage) {
    companion object {
        val ELEMENT_TYPE = IFileElementType("CWT_FILE", CwtLanguage)
    }

    override fun getFileType() = viewProvider.fileType

    val block get() = findChild<CwtRootBlock>()

    val propertyList get() = block?.propertyList.orEmpty()

    val valueList get() = block?.valueList.orEmpty()
}
