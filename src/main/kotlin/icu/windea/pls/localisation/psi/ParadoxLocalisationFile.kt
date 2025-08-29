package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.navigation.ParadoxLocalisationItemPresentation
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage) {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_LOCALISATION_FILE", ParadoxLocalisationLanguage)
    }

    val propertyLists: List<ParadoxLocalisationPropertyList>
        get() = findChildrenByClass(ParadoxLocalisationPropertyList::class.java).toList()

    val propertyList: ParadoxLocalisationPropertyList?
        get() = findChild<ParadoxLocalisationPropertyList>()?.takeIf { it.nextSibling !is ParadoxLocalisationPropertyList }

    val properties: List<ParadoxLocalisationProperty>
        get() = propertyList?.propertyList ?: emptyList()

    override fun getFileType() = ParadoxLocalisationFileType

    override fun getPresentation() = ParadoxLocalisationItemPresentation(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxLocalisationFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}
