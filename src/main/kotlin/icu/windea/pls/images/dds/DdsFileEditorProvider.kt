package icu.windea.pls.images.dds

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.images.editor.ImageFileEditorImpl

//org.intellij.images.editor.impl.ImageFileEditorProvider

/**
 * 用于提供DDS图片的编辑器页面，如同普通图片一样。如果要编辑图片，需要使用外部编辑器。
 */
class DdsFileEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == DdsFileType
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return ImageFileEditorImpl(project, file)
    }

    override fun getEditorTypeId(): String {
        return "dds"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}
