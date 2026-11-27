package icu.windea.pls.images.editor

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.application
import icu.windea.pls.images.ImageService
import org.intellij.images.editor.ImageDocument
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.editor.ImageEditor
import org.intellij.images.editor.ImageZoomModel
import java.awt.Color
import javax.swing.JComponent

// org.intellij.images.editor.impl.ImageEditorImpl

class ImageEditorImpl(
    private val project: Project,
    private val file: VirtualFile,
    isEmbedded: Boolean = false,
    isOpaque: Boolean = false
) : ImageEditor {
    private val editorUI = ImageEditorUI(this, isEmbedded, isOpaque)
    private val imageFileLoader = project.service<ImageFileService>().createImageFileLoader(this)
    private var disposed = false

    init {
        Disposer.register(this, editorUI)

        application.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                for (event in events) {
                    when (event) {
                        is VFilePropertyChangeEvent -> propertyChanged(event)
                        is VFileContentChangeEvent -> contentsChanged(event)
                    }
                }
            }
        })

        Disposer.register(this, imageFileLoader)

        setValue(file)
    }

    fun setValue(file: VirtualFile?) {
        imageFileLoader.loadFile(file)
    }

    fun setImageProvider(imageProvider: ScaledImageProvider?, format: String?) {
        editorUI.setImageProvider(imageProvider, format)
    }

    override fun isValid(): Boolean {
        val document: ImageDocument = editorUI.imageComponent.document
        return document.value != null
    }

    override fun getComponent(): ImageEditorUI {
        return editorUI
    }

    override fun getContentComponent(): JComponent {
        return editorUI.imageComponent
    }

    override fun getFile(): VirtualFile {
        return file
    }

    override fun getProject(): Project {
        return project
    }

    override fun getDocument(): ImageDocument {
        return editorUI.imageComponent.document
    }

    override fun setTransparencyChessboardVisible(visible: Boolean) {
        editorUI.imageComponent.isTransparencyChessboardVisible = visible
        editorUI.repaint()
    }

    override fun isTransparencyChessboardVisible(): Boolean {
        return editorUI.imageComponent.isTransparencyChessboardVisible
    }

    override fun isEnabledForActionPlace(place: String): Boolean {
        // Disable for thumbnails action
        return place != "Images.Thumbnails"
    }

    override fun setGridVisible(visible: Boolean) {
        editorUI.imageComponent.isGridVisible = visible
        editorUI.repaint()
    }

    override fun setEditorBackground(color: Color?) {
        editorUI.imageComponent.parent.background = color
    }

    override fun setBorderVisible(visible: Boolean) {
        editorUI.imageComponent.isBorderVisible = visible
    }

    override fun isGridVisible(): Boolean {
        return editorUI.imageComponent.isGridVisible
    }

    override fun isDisposed(): Boolean {
        return disposed
    }

    override fun getZoomModel(): ImageZoomModel {
        return editorUI.zoomModel
    }

    override fun dispose() {
        disposed = true
    }

    fun propertyChanged(event: VFilePropertyChangeEvent) {
        if (file != event.file) return

        // Change document
        file.refresh(true, false) {
            if (ImageService.getInstance().isExtendedImageFileType(file.fileType)) {
                setValue(file)
            } else {
                setValue(null)
                // Close editor
                val editorManager = FileEditorManager.getInstance(project)
                editorManager.closeFile(file)
            }
        }
    }

    fun contentsChanged(event: VFileContentChangeEvent) {
        if (file != event.file) return

        // Change document
        val postRunnable = Runnable { setValue(file) }
        RefreshQueue.getInstance().refresh(true, false, postRunnable, ModalityState.current(), file)
    }
}
