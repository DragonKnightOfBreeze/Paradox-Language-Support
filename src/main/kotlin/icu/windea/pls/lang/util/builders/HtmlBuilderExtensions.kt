@file:Suppress("unused")

package icu.windea.pls.lang.util.builders

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.builders.HtmlBuilder
import java.nio.file.Path

fun HtmlBuilder.appendLink(href: String, label: String, escapeLabel: Boolean = true): HtmlBuilder {
    append("<a href=\"").append(href.escapeXml()).append("\">")
    if (escapeLabel) append(label.escapeXml()) else append(label)
    append("</a>")
    return this
}

fun HtmlBuilder.appendImgTag(url: String): HtmlBuilder {
    append("<img src=\"").append(url.escapeXml()).append("\"/>")
    return this
}

fun HtmlBuilder.appendImgTag(url: String, width: Int, height: Int): HtmlBuilder {
    append("<img src=\"").append(url.escapeXml()).append("\"")
    append(" width=\"").append(width).append("\" height=\"").append(height).append("\" vspace=\"0\" hspace=\"0\"")
    append("/>")
    return this
}

/**
 * 构建可用于 HTML 的文件链接：
 *
 * - `file:///D:/Some/file.txt`
 * - `file:///D:/Some/file.txt:0:0-0:0`
 */
fun PsiElement.toFileLink(withRange: Boolean = true): String? {
    val file = containingFile?.virtualFile ?: return null

    // NOTE:
    // - `VirtualFile.toNioPath()` may throw for non-local files (e.g. jar://).
    // - We try to still produce a usable `file:///...` link when possible.
    val uri = runCatching { file.toNioPath().toUri().toString() }.getOrNull()
        ?: runCatching { VfsUtilCore.virtualToIoFile(file).toURI().toString() }.getOrNull()
        ?: runCatching { Path.of(file.path).toUri().toString() }.getOrNull()
        ?: file.url.takeIf { it.startsWith("file:") }
        ?: return null
    if (!withRange) return uri

    val range = textRange ?: return uri
    val document = PsiDocumentManager.getInstance(project).getDocument(containingFile) ?: return uri
    val start = document.toLineCol(range.startOffset)
    val end = document.toLineCol(range.endOffset)
    return "$uri:${start.line}:${start.col}-${end.line}:${end.col}"
}

private data class LineCol(val line: Int, val col: Int)

private fun com.intellij.openapi.editor.Document.toLineCol(offset: Int): LineCol {
    val line = getLineNumber(offset)
    val col = offset - getLineStartOffset(line)
    return LineCol(line, col)
}
