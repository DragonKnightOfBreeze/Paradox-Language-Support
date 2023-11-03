package icu.windea.pls.core

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.script.psi.*

interface AttributesKeyAware {
    fun getAttributesKey(): TextAttributesKey? = null
    
    fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? = null
}

interface PsiReferencesAware {
    fun getReferences(): Array<out PsiReference>? = null
}
