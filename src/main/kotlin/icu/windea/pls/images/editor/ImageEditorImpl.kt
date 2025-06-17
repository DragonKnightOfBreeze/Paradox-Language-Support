package icu.windea.pls.images.editor

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.*
import icu.windea.pls.images.*
import org.intellij.images.editor.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.thumbnail.actionSystem.*
import java.awt.*
import javax.swing.*

//org.intellij.images.editor.impl.ImageEditorImpl

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

        @Suppress("DEPRECATION")
        VirtualFileManager.getInstance().addVirtualFileListener(object : VirtualFileListener {
            override fun propertyChanged(event: VirtualFilePropertyEvent) {
                this@ImageEditorImpl.propertyChanged(event)
            }

            override fun contentsChanged(event: VirtualFileEvent) {
                this@ImageEditorImpl.contentsChanged(event)
            }
        }, this)

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
        return ThumbnailViewActions.ACTION_PLACE != place
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

    fun propertyChanged(event: VirtualFilePropertyEvent) {
        if (file != event.file) return

        // Change document
        file.refresh(true, false) {
            if (ImageManager.isExtendedImageFileType(file.fileType)) {
                setValue(file)
            } else {
                setValue(null)
                // Close editor
                val editorManager = FileEditorManager.getInstance(project)
                editorManager.closeFile(file)
            }
        }
    }

    fun contentsChanged(event: VirtualFileEvent) {
        if (file != event.file) return

        // Change document
        val postRunnable = Runnable { setValue(file) }
        RefreshQueue.getInstance().refresh(true, false, postRunnable, ModalityState.current(), file)
    }
}
