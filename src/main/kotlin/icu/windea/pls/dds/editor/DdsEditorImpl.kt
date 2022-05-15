package icu.windea.pls.dds.editor

import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.*
import icu.windea.pls.dds.*
import org.intellij.images.editor.*
import org.intellij.images.thumbnail.actionSystem.*
import java.awt.*
import javax.swing.*

//org.intellij.images.editor.impl.ImageEditorImpl

private const val DDS_FORMAT = "dds"

@Suppress("DEPRECATION")
class DdsEditorImpl(
	private val project: Project,
	private val file: VirtualFile,
	isEmbedded: Boolean = false,
	isOpaque: Boolean = true
) : ImageEditor {
	private val editorUI = DdsEditorUI(this, isEmbedded, isOpaque)
	private var disposed = false
	
	init {
		Disposer.register(this, editorUI)
		
		VirtualFileManager.getInstance().addVirtualFileListener(object : VirtualFileListener {
			override fun propertyChanged(event: VirtualFilePropertyEvent) {
				this@DdsEditorImpl.propertyChanged(event)
			}
			
			override fun contentsChanged(event: VirtualFileEvent) {
				this@DdsEditorImpl.contentsChanged(event)
			}
		}, this)
		
		setValue(file)
	}
	
	fun setValue(file: VirtualFile?) {
		if(file == null) {
			editorUI.setImageProvider(null, null)
			return
		}
		try {
			val imageProvider = file.getImageProvider()
			if(imageProvider == null) {
				editorUI.setImageProvider(null, null)
				return
			}
			editorUI.setImageProvider(imageProvider, DDS_FORMAT)
		} catch(e: Exception) {
			//Error loading image file
			editorUI.setImageProvider(null, null)
		}
	}
	
	override fun isValid(): Boolean {
		val document: ImageDocument = editorUI.getImageComponent().document
		return document.value != null
	}
	
	override fun getComponent(): DdsEditorUI {
		return editorUI
	}
	
	override fun getContentComponent(): JComponent {
		return editorUI.getImageComponent()
	}
	
	override fun getFile(): VirtualFile {
		return file
	}
	
	override fun getProject(): Project {
		return project
	}
	
	override fun getDocument(): ImageDocument {
		return editorUI.getImageComponent().document
	}
	
	override fun setTransparencyChessboardVisible(visible: Boolean) {
		editorUI.getImageComponent().isTransparencyChessboardVisible = visible
		editorUI.repaint()
	}
	
	override fun isTransparencyChessboardVisible(): Boolean {
		return editorUI.getImageComponent().isTransparencyChessboardVisible
	}
	
	override fun isEnabledForActionPlace(place: String): Boolean {
		// Disable for thumbnails action
		return ThumbnailViewActions.ACTION_PLACE != place
	}
	
	override fun setGridVisible(visible: Boolean) {
		editorUI.getImageComponent().isGridVisible = visible
		editorUI.repaint()
	}
	
	override fun setEditorBackground(color: Color?) {
		editorUI.getImageComponent().parent.background = color
	}
	
	override fun setBorderVisible(visible: Boolean) {
		editorUI.getImageComponent().isBorderVisible = visible
	}
	
	override fun isGridVisible(): Boolean {
		return editorUI.getImageComponent().isGridVisible
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
		if(file == event.file) {
			// Change document
			file.refresh(true, false) {
				if(file.isDdsFileType()) {
					setValue(file)
				} else {
					setValue(null)
					// Close editor
					val editorManager = FileEditorManager.getInstance(project)
					editorManager.closeFile(file)
				}
			}
		}
	}
	
	fun contentsChanged(event: VirtualFileEvent) {
		if(file == event.file) {
			// Change document
			refreshFile()
		}
	}
	
	fun refreshFile() {
		val postRunnable = Runnable { setValue(file) }
		RefreshQueue.getInstance().refresh(true, false, postRunnable, ModalityState.current(), file)
	}
}
