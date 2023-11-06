package icu.windea.pls.cwt.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*

object CwtElementFactory {
    @JvmStatic
    fun createDummyFile(project: Project, text: String): CwtFile {
        return PsiFileFactory.getInstance(project).createFileFromText(CwtLanguage, text).cast()
    }
    
    @JvmStatic
    fun createLine(project: Project): PsiElement {
        return createDummyFile(project, "\n").firstChild
    }
    
    fun createRootBlock(project: Project, text: String): CwtRootBlock {
        return createDummyFile(project, text).findChild()!!
    }
    
    @JvmStatic
    fun createProperty(project: Project, key: String, value: String): CwtProperty {
        val usedKey = key.quoteIfNecessary()
        return createRootBlock(project, "$usedKey=$value").findChild()!!
    }
    
    @JvmStatic
    fun createPropertyKey(project: Project, key: String): CwtPropertyKey {
        return createProperty(project, key, "0").findChild()!!
    }
    
    @JvmStatic
    fun createValue(project: Project, value: String): CwtValue {
        return createProperty(project, "a", value).findChild()!!
    }
    
    @JvmStatic
    fun createString(project: Project, value: String): CwtString {
        val usedValue = value.quoteIfNecessary()
        return createValue(project, usedValue).cast()!!
    }
}