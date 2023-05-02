package icu.windea.pls.core.psi

import com.intellij.openapi.editor.colors.*

interface AttributesKeyAware {
    fun getAttributesKey(): TextAttributesKey? {
        return null
    }
}