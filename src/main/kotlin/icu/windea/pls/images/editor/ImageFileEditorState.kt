package icu.windea.pls.images.editor

import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.TransferableFileEditorState
import java.io.Serializable

//org.intellij.images.editor.impl.ImageFileEditorState

class ImageFileEditorState(
    var isBackgroundVisible: Boolean,
    var isGridVisible: Boolean,
    var zoomFactor: Double,
    var isZoomFactorChanged: Boolean
) : TransferableFileEditorState, Serializable {
    companion object {
        private const val IMAGE_EDITOR_ID = "PlsImageEditor"
        private const val BACKGROUND_VISIBLE_OPTION = "backgroundVisible"
        private const val GRID_VISIBLE_OPTION = "gridVisible"
        private const val ZOOM_FACTOR_OPTION = "zoomFactor"
        private const val ZOOM_FACTOR_CHANGED_OPTION = "zoomFactorChanged"
    }

    override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean {
        return otherState is ImageFileEditorState
    }

    override fun setCopiedFromMasterEditor() {
        isZoomFactorChanged = true
    }

    override fun getEditorId(): String {
        return IMAGE_EDITOR_ID
    }

    override fun getTransferableOptions(): Map<String, String> {
        val map: HashMap<String, String> = HashMap()
        map[BACKGROUND_VISIBLE_OPTION] = isBackgroundVisible.toString()
        map[GRID_VISIBLE_OPTION] = isGridVisible.toString()
        map[ZOOM_FACTOR_OPTION] = zoomFactor.toString()
        map[ZOOM_FACTOR_CHANGED_OPTION] = isZoomFactorChanged.toString()
        return map
    }

    override fun setTransferableOptions(options: Map<String?, String?>) {
        var o = options[BACKGROUND_VISIBLE_OPTION]
        if (o != null) {
            isBackgroundVisible = java.lang.Boolean.parseBoolean(o)
        }
        o = options[GRID_VISIBLE_OPTION]
        if (o != null) {
            isGridVisible = java.lang.Boolean.parseBoolean(o)
        }
        o = options[ZOOM_FACTOR_OPTION]
        if (o != null) {
            zoomFactor = o.toDouble()
        }
        o = options[ZOOM_FACTOR_CHANGED_OPTION]
        if (o != null) {
            isZoomFactorChanged = java.lang.Boolean.parseBoolean(o)
        }
    }
}
