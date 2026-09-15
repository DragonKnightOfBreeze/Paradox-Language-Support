package icu.windea.pls.cwt.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtFile

class CwtFileImpl(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, CwtLanguage), CwtFile {
    override val block get() = CwtPsiImplUtil.getBlock(this)

    override val memberContainer get() = CwtPsiImplUtil.getMemberContainer(this)

    override val members get() = CwtPsiImplUtil.getMembers(this)

    override fun getPresentation() = CwtPsiImplUtil.getPresentation(this)

    override fun getResolveScope() = CwtPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = CwtPsiImplUtil.getUseScope(this)

    override fun toString() = CwtPsiImplUtil.toString(this)
}
