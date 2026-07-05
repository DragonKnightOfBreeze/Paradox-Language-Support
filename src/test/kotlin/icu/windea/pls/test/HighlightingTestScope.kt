package icu.windea.pls.test

@Suppress("unused")
interface HighlightingTestScope {
    data class Tag(val start: String, val end: String)

    fun String.toTag(level: String) = Tag("<$level descr=\"${this.replace("\"", "\\\\\"")}\">", "</$level>")

    fun String.toErrorTag() = toTag("error")

    fun String.toWarningTag() = toTag("warning")

    fun String.toWeakWarningTag() = toTag("weak_warning")

    fun String.toInfoTag() = toTag("info")
}
