package icu.windea.pls.images.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.VirtualFile

interface ImageFileLoader : Disposable {
    fun loadFile(file: VirtualFile?)
}
