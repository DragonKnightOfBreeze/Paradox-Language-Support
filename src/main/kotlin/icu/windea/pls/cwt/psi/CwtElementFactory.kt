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
    fun createProperty(project: Project, key: String, value: String): CwtProperty {
        val newKey = buildString { ParadoxEscapeManager.escapeCwtExpression(key, this) }.quoteIfNecessary()
        val newValue = buildString { ParadoxEscapeManager.escapeCwtExpression(value, this) }.quoteIfNecessary()
        return createRootBlock(project, "$newKey = $newValue").findChild()!!
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
        return createValue(project, value).castOrNull<CwtString>()
            ?: createValue(project, value.quote()).cast()
    }
}