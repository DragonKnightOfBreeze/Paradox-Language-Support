package icu.windea.pls.core.documentation

interface DocumentationBuilder {
    val content: StringBuilder
    
    fun append(string: String) = apply { content.append(string) }
    
    fun append(value: Any?) = apply { content.append(value) }
    
    companion object {
        inline fun buildDocumentation(builderAction: DocumentationBuilder.() -> Unit): String {
            val builder = Default()
            builder.builderAction()
            return builder.content.toString()
        }
    }
    
    class Default : DocumentationBuilder {
        override val content: StringBuilder = StringBuilder()
    }
}

