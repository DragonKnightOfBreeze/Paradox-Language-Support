package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.model.*

class ParadoxLocalisationFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage) {
    companion object {
        val ELEMENT_TYPE: IFileElementType = ParadoxLocalisationFileStubElementType.INSTANCE
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
