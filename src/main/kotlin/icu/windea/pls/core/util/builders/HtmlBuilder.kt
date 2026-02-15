package icu.windea.pls.core.util.builders

fun buildHtml(): HtmlBuilder {
    return HtmlBuilderImpl()
}

@Suppress("unused")
inline fun buildHtml(block: HtmlBuilder.() -> Unit): String {
    val builder = buildHtml()
    builder.block()
    return builder.toString()
}

interface HtmlBuilder {
    val content: StringBuilder

    fun append(string: String): HtmlBuilder

    fun append(value: Any?): HtmlBuilder

    override fun toString(): String
}

// region Implementations

private class HtmlBuilderImpl : HtmlBuilder {
    override val content: StringBuilder = StringBuilder()

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()
}

// endregion
