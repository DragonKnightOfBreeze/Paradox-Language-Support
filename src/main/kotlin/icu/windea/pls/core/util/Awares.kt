package icu.windea.pls.core.util

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*

interface AttributesKeyAware {
    fun getAttributesKey(): TextAttributesKey? {
        return null
    }
}

interface PsiReferencesAware {
    fun getReferences(): Array<out PsiReference>? {
        return null
    }
}