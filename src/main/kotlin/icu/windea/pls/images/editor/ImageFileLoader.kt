package icu.windea.pls.images.editor

import com.intellij.openapi.*
import com.intellij.openapi.vfs.*

interface ImageFileLoader : Disposable {
    fun loadFile(file: VirtualFile?)
}
