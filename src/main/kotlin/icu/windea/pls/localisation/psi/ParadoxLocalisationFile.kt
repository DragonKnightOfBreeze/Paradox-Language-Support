package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

interface ParadoxLocalisationFile : PsiFile, ParadoxFile {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_LOCALISATION_FILE", ParadoxLocalisationLanguage)
    }

    val propertyLists: List<ParadoxLocalisationPropertyList>

    val propertyList: ParadoxLocalisationPropertyList?

    val properties: List<ParadoxLocalisationProperty>

    override fun getFileType() = ParadoxLocalisationFileType
}
