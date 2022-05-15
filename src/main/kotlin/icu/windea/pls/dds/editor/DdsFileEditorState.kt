package icu.windea.pls.dds.editor

import com.intellij.openapi.fileEditor.*
import java.io.*

//org.intellij.images.editor.impl.ImageFileEditorState

private const val DDS_EDITOR_ID = "DdsEditor"
private const val BACKGROUND_VISIBLE_OPTION = "backgroundVisible"
private const val GRID_VISIBLE_OPTION = "gridVisible"
private const val ZOOM_FACTOR_OPTION = "zoomFactor"
private const val ZOOM_FACTOR_CHANGED_OPTION = "zoomFactorChanged"

class DdsFileEditorState(
	var isBackgroundVisible: Boolean,
	var isGridVisible: Boolean,
	var zoomFactor: Double,
	var isZoomFactorChanged: Boolean
) : TransferableFileEditorState, Serializable {
	override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean {
		return otherState is DdsFileEditorState
	}
	
	override fun setCopiedFromMasterEditor() {
		isZoomFactorChanged = true
	}
	
	override fun getEditorId(): String {
		return DDS_EDITOR_ID
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
		if(o != null) {
			isBackgroundVisible = java.lang.Boolean.parseBoolean(o)
		}
		o = options[GRID_VISIBLE_OPTION]
		if(o != null) {
			isGridVisible = java.lang.Boolean.parseBoolean(o)
		}
		o = options[ZOOM_FACTOR_OPTION]
		if(o != null) {
			zoomFactor = o.toDouble()
		}
		o = options[ZOOM_FACTOR_CHANGED_OPTION]
		if(o != null) {
			isZoomFactorChanged = java.lang.Boolean.parseBoolean(o)
		}
	}
}