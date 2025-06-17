package icu.windea.pls.images.editor

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import org.intellij.images.editor.*
import org.intellij.images.options.*
import java.beans.*
import javax.swing.*

//org.intellij.images.editor.impl.ImageFileEditorImpl

class ImageFileEditorImpl(
    project: Project,
    file: VirtualFile
) : UserDataHolderBase(), ImageFileEditor, PropertyChangeListener {
    companion object {
        private const val NAME = "Pls.ImageFileEditor"
    }

    private val imageEditor = ImageEditorImpl(project, file)
    private val dispatcher = EventDispatcher.create(PropertyChangeListener::class.java)

    init {
        Disposer.register(this, imageEditor)

        // Set background and grid default options
        val options = OptionsManager.getInstance().options
        val editorOptions = options.editorOptions
        val gridOptions = editorOptions.gridOptions
        val transparencyChessboardOptions = editorOptions.transparencyChessboardOptions
        imageEditor.setGridVisible(gridOptions.isShowDefault)
        imageEditor.setTransparencyChessboardVisible(transparencyChessboardOptions.isShowDefault)

        imageEditor.component.imageComponent.addPropertyChangeListener(this)
    }

    override fun getComponent(): JComponent {
        return imageEditor.component
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return imageEditor.contentComponent
    }

    override fun getName(): String {
        return NAME
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        val zoomModel = imageEditor.zoomModel
        return ImageFileEditorState(
            imageEditor.isTransparencyChessboardVisible,
            imageEditor.isGridVisible,
            zoomModel.zoomFactor,
            zoomModel.isZoomLevelChanged
        )
    }

    override fun setState(state: FileEditorState) {
        if (state is ImageFileEditorState) {
            val options = OptionsManager.getInstance().options
            val zoomOptions = options.editorOptions.zoomOptions
            val editorState = state
            val zoomModel = imageEditor.zoomModel
            imageEditor.isTransparencyChessboardVisible = editorState.isBackgroundVisible
            imageEditor.isGridVisible = editorState.isGridVisible
            if (editorState.isZoomFactorChanged || !zoomOptions.isSmartZooming) {
                zoomModel.zoomFactor = editorState.zoomFactor
            }
            zoomModel.isZoomLevelChanged = editorState.isZoomFactorChanged
        }
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        dispatcher.addListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        dispatcher.removeListener(listener)
    }

    override fun propertyChange(event: PropertyChangeEvent) {
        val editorEvent = PropertyChangeEvent(this, event.propertyName, event.oldValue, event.newValue)
        dispatcher.multicaster.propertyChange(editorEvent)
    }

    override fun dispose() {}

    override fun getImageEditor(): ImageEditor {
        return imageEditor
    }

    override fun getFile(): VirtualFile {
        return imageEditor.file
    }
}
