package icu.windea.pls.core.documentation

interface DocumentationBuilder {
    val content: StringBuilder
    
    fun append(string: String) = content.append(string)
    
    fun append(value: Any?) = content.append(value)
    
    companion object {
        fun buildDocumentation(): DocumentationBuilder = Default()
    }
    
    private class Default : DocumentationBuilder {
        override val content: StringBuilder = StringBuilder()
    }
}

