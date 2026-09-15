package icu.windea.pls.cwt.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.impl.CwtPsiImplUtil

class CwtFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage), CwtMemberContext {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("CWT_FILE", CwtLanguage)
    }

    val block: CwtRootBlock? get() = CwtPsiImplUtil.getBlock(this)

    override val memberContainer: CwtRootBlock? get() = CwtPsiImplUtil.getMemberContainer(this)

    override val members: List<CwtMember> get() = CwtPsiImplUtil.getMembers(this)

    override fun getFileType() = CwtFileType

    override fun getPresentation() = CwtElementPresentation(this)

    override fun getResolveScope() = CwtPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = CwtPsiImplUtil.getUseScope(this)

    override fun toString() = CwtPsiImplUtil.toString(this)
}
