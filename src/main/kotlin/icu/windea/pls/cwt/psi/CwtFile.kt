package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.navigation.CwtItemPresentation
import icu.windea.pls.cwt.psi.impl.CwtPsiImplUtil
import icu.windea.pls.lang.psi.PlsPsiManager

class CwtFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage), CwtMemberContainer {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = IFileElementType("CWT_FILE", CwtLanguage)
    }

    val block: CwtRootBlock? get() = findChild<_>()
    override val members: List<CwtMember> get() = CwtPsiImplUtil.getMembers(this)

    override fun getFileType() = CwtFileType

    override fun getPresentation() = CwtItemPresentation(this)

    override fun toString() = PlsPsiManager.toPresentableString(this)
}
