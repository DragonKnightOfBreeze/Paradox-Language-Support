package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.navigation.*

class ParadoxLocalisationFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage) {
    override fun getFileType() = ParadoxLocalisationFileType
    
    val propertyLists: List<ParadoxLocalisationPropertyList>
        get() = findChildrenByClass(ParadoxLocalisationPropertyList::class.java).toList()
    
    val propertyList: ParadoxLocalisationPropertyList?
        get() = findChild<ParadoxLocalisationPropertyList>()?.takeIf { it.nextSibling !is ParadoxLocalisationPropertyList }
    
    val properties: List<ParadoxLocalisationProperty>
        get() = propertyList?.propertyList ?: emptyList()
    
    fun getLocaleIdFromFileName(): String? {
        if(!name.endsWith(".yml", true)) return null
        val dotIndex = name.lastIndexOf('.').let { if(it == -1) name.lastIndex else it }
        val prefixIndex = name.lastIndexOf("l_", dotIndex)
        if(prefixIndex == -1) return null
        return name.substring(prefixIndex, name.length - 4)
    }
    
    fun getExpectedFileName(localeId: String): String {
        val dotIndex = name.lastIndexOf('.').let { if(it == -1) name.lastIndex else it }
        val prefixIndex = name.lastIndexOf("l_", dotIndex)
        if(prefixIndex == -1) {
            return name.substring(0, dotIndex) + localeId + ".yml"
        } else {
            return name.substring(0, prefixIndex) + localeId + ".yml"
        }
    }
    
    override fun getPresentation(): ItemPresentation {
        return ParadoxLocalisationFilePresentation(this)
    }
    
    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || (another is ParadoxLocalisationFile && fileInfo == another.fileInfo)
    }
    
    //缓存语言区域
    @Volatile private var _locale: CwtLocalisationLocaleConfig? = null
    val locale: CwtLocalisationLocaleConfig?
        get() = _locale ?: selectLocale(propertyList?.locale).also { _locale = it }
    
    override fun subtreeChanged() {
        _locale = null
        super.subtreeChanged()
    }
}
