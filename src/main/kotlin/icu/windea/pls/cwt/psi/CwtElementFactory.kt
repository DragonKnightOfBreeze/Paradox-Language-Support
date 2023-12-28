package icu.windea.pls.cwt.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.util.*

object CwtElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): CwtFile {
        return PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage, text).cast()
    }
    
    fun createRootBlock(project: Project, text: String): CwtRootBlock {
        return createDummyFile(project, text).findChild()!!
    }
    
    @JvmStatic
    fun createPropertyFromText(project: Project, text: String): CwtProperty {
        return createRootBlock(project, text).findChild()!!
    }
    
    @JvmStatic
    fun createProperty(project: Project, key: String, value: String): CwtProperty {
        val newKey = key.quoteIfNecessary(or = key.isQuoted())
        val newValue = value.quoteIfNecessary(or = value.isQuoted())
        return createRootBlock(project, "$newKey = $newValue").findChild()!!
    }
    
    @JvmStatic
    fun createPropertyKeyFromText(project: Project, text: String): CwtPropertyKey {
        return createPropertyFromText(project, "$text = v").findChild()!!
    }
    
    @JvmStatic
    fun createPropertyKey(project: Project, key: String): CwtPropertyKey {
        return createProperty(project, key, "0").findChild()!!
    }
    
    @JvmStatic
    fun createValueFromText(project: Project, text: String): CwtValue {
        return createPropertyFromText(project, "k = $text").findChild()!!
    }
    
    @JvmStatic
    fun createValue(project: Project, value: String): CwtValue {
        return createProperty(project, "a", value).findChild()!!
    }
    
    @JvmStatic
    fun createStringFromText(project: Project, text: String): CwtString {
        return createValueFromText(project, text).castOrNull<CwtString>()
            ?: createValueFromText(project, text.quote()).cast()
    }
    
    @JvmStatic
    fun createString(project: Project, value: String): CwtString {
        return createValue(project, value).castOrNull<CwtString>()
            ?: createValue(project, value.quote()).cast()
    }
    
    @JvmStatic
    fun createBlock(project: Project, value: String): CwtBlock {
        return createValue(project, value).cast()!!
    }
}