package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
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

    fun getLocaleIdFromFileName(): String? {
        if (!name.endsWith(".yml", true)) return null
        val dotIndex = name.lastIndexOf('.').let { if (it == -1) name.lastIndex else it }
        val prefixIndex = name.lastIndexOf("l_", dotIndex)
        if (prefixIndex == -1) return null
        return name.substring(prefixIndex, name.length - 4)
    }

    fun getExpectedFileName(localeId: String): String {
        val dotIndex = name.lastIndexOf('.').let { if (it == -1) name.lastIndex else it }
        val prefixIndex = name.lastIndexOf("l_", dotIndex)
        if (prefixIndex == -1) {
            return name.substring(0, dotIndex) + "_" + localeId + ".yml"
        } else {
            return name.substring(0, prefixIndex) + "_" + localeId + ".yml"
        }
    }

    override fun getPresentation(): ItemPresentation {
        return ParadoxLocalisationFilePresentation(this)
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || (another is ParadoxLocalisationFile && fileInfo == another.fileInfo)
    }
}
