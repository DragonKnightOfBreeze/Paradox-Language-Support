package icu.windea.pls.script.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptElementPresentation
import icu.windea.pls.script.psi.ParadoxScriptFile

class ParadoxScriptFileImpl(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxScriptFile {
    override val block get() = ParadoxScriptPsiImplUtil.getBlock(this)

    override val memberContainer get() = ParadoxScriptPsiImplUtil.getMemberContainer(this)

    override val members get() = ParadoxScriptPsiImplUtil.getMembers(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxScriptPsiImplUtil.isEquivalentTo(this, another)

    override fun getPresentation() = ParadoxScriptElementPresentation(this)

    override fun getResolveScope() = ParadoxScriptPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxScriptPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxScriptPsiImplUtil.toString(this)
}
