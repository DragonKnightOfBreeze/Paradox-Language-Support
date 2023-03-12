package icu.windea.pls.core.diff

import com.intellij.diff.contents.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import java.nio.charset.*

class FileDocumentReadonlyContent(
    project: Project?,
    document: Document,
    file: VirtualFile,
    private val original: VirtualFile,
): FileDocumentContentImpl(project, document, file) {
    override fun getLineSeparator(): LineSeparator? {
        val s = LoadTextUtil.detectLineSeparator(original, true) ?: return null
        return LineSeparator.fromString(s)
    }
    
    override fun getCharset(): Charset {
        return original.charset
    }
    
    override fun hasBom(): Boolean? {
        return original.bom != null
    }
}