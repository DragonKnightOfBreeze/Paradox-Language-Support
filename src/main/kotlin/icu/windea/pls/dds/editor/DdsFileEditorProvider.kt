package icu.windea.pls.dds.editor

import com.intellij.ide.highlighter.FileTypeRegistrar
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.dds.*

//org.intellij.images.editor.impl.ImageFileEditorProvider

private const val EDITOR_TYPE_ID = "dds"

/**
 * 用于提供DDS图片的编辑器页面，如同普通图片一样。如果要编辑DDS图片，需要使用外部编辑器。
 */
class DdsFileEditorProvider : FileEditorProvider { //NOT DumbAware
	override fun accept(project: Project, file: VirtualFile): Boolean {
		return FileTypeRegistry.getInstance().isFileOfType(file, DdsFileType) //参考的代码中如此
	}
	
	override fun createEditor(project: Project, file: VirtualFile): FileEditor {
		TODO("Not yet implemented")
	}
	
	override fun getEditorTypeId(): String {
		return EDITOR_TYPE_ID
	}
	
	override fun getPolicy(): FileEditorPolicy {
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR
	}
}