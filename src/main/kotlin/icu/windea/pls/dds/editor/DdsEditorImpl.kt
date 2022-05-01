package icu.windea.pls.dds.editor

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*

//org.intellij.images.editor.impl.ImageEditorImpl

class DdsEditorImpl(
	private val project: Project,
	private val file: VirtualFile,
	isEmbedded: Boolean = false,
	isOpaque: Boolean = true
) {
	private val editorUI = DdsEditorUI(this, isEmbedded, isOpaque)
	com.intellij.openapi.util.Disposer.register(this, editorUI)
}
