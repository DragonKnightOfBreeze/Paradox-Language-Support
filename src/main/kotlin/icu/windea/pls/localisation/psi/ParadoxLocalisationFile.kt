package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPsiImplUtil
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage), ParadoxFile {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_LOCALISATION_FILE", ParadoxLocalisationLanguage)
    }

    val propertyLists: List<ParadoxLocalisationPropertyList> get() = ParadoxLocalisationPsiImplUtil.getPropertyLists(this)

    val propertyList: ParadoxLocalisationPropertyList? get() = ParadoxLocalisationPsiImplUtil.getPropertyList(this)

    val properties: List<ParadoxLocalisationProperty> get() = ParadoxLocalisationPsiImplUtil.getProperties(this)

    override fun getFileType() = ParadoxLocalisationFileType

    override fun getPresentation() = ParadoxLocalisationPsiPresentation(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxLocalisationPsiImplUtil.isEquivalentTo(this, another)

    override fun getResolveScope() = ParadoxLocalisationPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxLocalisationPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxLocalisationPsiImplUtil.toString(this)
}
