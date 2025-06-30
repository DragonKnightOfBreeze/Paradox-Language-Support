package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.ParadoxLocalisationFileManager
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.model.*

class ParadoxLocalisationFile(
    viewProvider: FileViewProvider,
    val gameType: ParadoxGameType? = null
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage) {
    override fun getFileType() = ParadoxLocalisationFileType

    val propertyLists: List<ParadoxLocalisationPropertyList>
        get() = findChildrenByClass(ParadoxLocalisationPropertyList::class.java).toList()

    val propertyList: ParadoxLocalisationPropertyList?
        get() = findChild<ParadoxLocalisationPropertyList>()?.takeIf { it.nextSibling !is ParadoxLocalisationPropertyList }

    val properties: List<ParadoxLocalisationProperty>
        get() = propertyList?.propertyList ?: emptyList()

    override fun getPresentation(): ItemPresentation {
        return ParadoxLocalisationFilePresentation(this)
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || (another is ParadoxLocalisationFile && fileInfo == another.fileInfo)
    }
}
