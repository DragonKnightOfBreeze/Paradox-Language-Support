package icu.windea.pls.core.documentation

class DocumentationBuilder {
    val content: StringBuilder = StringBuilder()
    
    fun append(string: String) = apply { content.append(string) }
    
    fun append(value: Any?) = apply { content.append(value) }
}

