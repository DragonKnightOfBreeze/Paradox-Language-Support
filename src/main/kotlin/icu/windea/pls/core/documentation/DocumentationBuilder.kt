package icu.windea.pls.core.documentation

import com.intellij.openapi.util.*
import icu.windea.pls.core.util.*

class DocumentationBuilder : UserDataHolderBase() {
    val content: StringBuilder = StringBuilder()
    
    fun append(string: String) = apply { content.append(string) }
    
    fun append(value: Any?) = apply { content.append(value) }
    
    override fun toString(): String {
        return content.toString()
    }
    
    object Keys : KeyRegistry()
}

