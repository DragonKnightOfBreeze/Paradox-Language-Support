package icu.windea.pls.dds.editor

import com.intellij.codeHighlighting.*
import com.intellij.ide.structureView.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import org.intellij.images.editor.*
import org.intellij.images.options.*
import java.beans.*
import javax.swing.*

private const val NAME = "DdsFileEditor"

//org.intellij.images.editor.impl.ImageFileEditorImpl

class DdsFileEditorImpl(
	project: Project,
	file: VirtualFile
) : UserDataHolderBase(), ImageFileEditor, PropertyChangeListener {
	private val ddsEditor = DdsEditorImpl(project, file) 
	private val dispatcher = EventDispatcher.create(PropertyChangeListener::class.java)
	
	init {
		Disposer.register(this, ddsEditor)
	}
	
	override fun getComponent(): JComponent {
		return ddsEditor.component
	}
	
	override fun getPreferredFocusedComponent(): JComponent {
		return ddsEditor.contentComponent
	}
	
	override fun getName(): String {
		return NAME
	}
	
	override fun getState(level: FileEditorStateLevel): FileEditorState {
		val zoomModel = ddsEditor.zoomModel
		return DdsFileEditorState(
			ddsEditor.isTransparencyChessboardVisible,
			ddsEditor.isGridVisible,
			zoomModel.zoomFactor,
			zoomModel.isZoomLevelChanged)
	}
	
	override fun setState(state: FileEditorState) {
		if(state is DdsFileEditorState) {
			val options = OptionsManager.getInstance().options
			val zoomOptions = options.editorOptions.zoomOptions
			val editorState = state
			val zoomModel = ddsEditor.zoomModel
			ddsEditor.isTransparencyChessboardVisible = editorState.isBackgroundVisible
			ddsEditor.isGridVisible = editorState.isGridVisible
			if(editorState.isZoomFactorChanged || !zoomOptions.isSmartZooming) {
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
	
	override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
		return null
	}
	
	override fun getCurrentLocation(): FileEditorLocation? {
		return null
	}
	
	override fun getStructureViewBuilder(): StructureViewBuilder? {
		return null
	}
	
	override fun dispose() {}
	
	override fun getImageEditor(): ImageEditor {
		return ddsEditor
	}
	
	override fun getFile(): VirtualFile {
		return ddsEditor.file
	}
}