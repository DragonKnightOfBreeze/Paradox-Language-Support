package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage

interface CwtFile : PsiFile, CwtMemberContext {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("CWT_FILE", CwtLanguage)
    }

    val block: CwtRootBlock?

    override fun getFileType() = CwtFileType
}
