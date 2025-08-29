package icu.windea.pls.lang.diff

import com.intellij.diff.contents.FileDocumentContentImpl
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.LineSeparator
import java.nio.charset.Charset

class FileDocumentReadonlyContent(
    project: Project?,
    document: Document,
    file: VirtualFile,
    private val original: VirtualFile,
) : FileDocumentContentImpl(project, document, file) {
    override fun getLineSeparator(): LineSeparator? {
        val s = LoadTextUtil.detectLineSeparator(original, true) ?: return null
        return LineSeparator.fromString(s)
    }

    override fun getCharset(): Charset {
        return original.charset
    }

    override fun hasBom(): Boolean {
        return original.bom != null
    }
}
