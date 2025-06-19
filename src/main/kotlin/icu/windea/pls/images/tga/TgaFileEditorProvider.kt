package icu.windea.pls.images.tga

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.images.editor.*

//org.intellij.images.editor.impl.ImageFileEditorProvider

/**
 * 用于提供TGA图片的编辑器页面，如同普通图片一样。如果要编辑图片，需要使用外部编辑器。
 */
class TgaFileEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == TgaFileType
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return ImageFileEditorImpl(project, file)
    }

    override fun getEditorTypeId(): String {
        return "tga"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}
