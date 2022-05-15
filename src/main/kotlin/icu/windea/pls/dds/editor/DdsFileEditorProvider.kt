package icu.windea.pls.dds.editor

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*

//org.intellij.images.editor.impl.ImageFileEditorProvider

/**
 * 用于提供DDS图片的编辑器页面，如同普通图片一样。如果要编辑DDS图片，需要使用外部编辑器。
 */
class DdsFileEditorProvider : FileEditorProvider, DumbAware {
	override fun accept(project: Project, file: VirtualFile): Boolean {
		return file.isDdsFileType()
	}
	
	override fun createEditor(project: Project, file: VirtualFile): FileEditor {
		return DdsFileEditorImpl(project, file)
	}
	
	override fun getEditorTypeId(): String {
		return "dds"
	}
	
	override fun getPolicy(): FileEditorPolicy {
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR
	}
}