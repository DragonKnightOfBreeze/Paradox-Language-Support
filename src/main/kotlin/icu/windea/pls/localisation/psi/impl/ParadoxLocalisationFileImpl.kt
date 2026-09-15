package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class ParadoxLocalisationFileImpl(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage), ParadoxLocalisationFile {
    override val propertyLists get() = ParadoxLocalisationPsiImplUtil.getPropertyLists(this)

    override val propertyList get() = ParadoxLocalisationPsiImplUtil.getPropertyList(this)

    override val properties get() = ParadoxLocalisationPsiImplUtil.getProperties(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxLocalisationPsiImplUtil.isEquivalentTo(this, another)

    override fun getPresentation() = ParadoxLocalisationPsiImplUtil.getPresentation(this)

    override fun getResolveScope() = ParadoxLocalisationPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxLocalisationPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxLocalisationPsiImplUtil.toString(this)
}
