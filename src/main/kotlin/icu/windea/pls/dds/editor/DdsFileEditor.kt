package icu.windea.pls.dds.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.*
import org.intellij.images.editor.*
import org.intellij.images.ui.ImageComponentDecorator
import java.beans.PropertyChangeListener

private const val NAME = "DdsFileEditor"

//org.intellij.images.editor.impl.ImageFileEditorImpl

class DdsFileEditor(
	project: Project,
	file: VirtualFile
) : UserDataHolderBase(), Disposable, ImageComponentDecorator, PropertyChangeListener {
	private val ddsEditor = DdsEditorImpl(project, file) 
	private val dispatcher = EventDispatcher.create(PropertyChangeListener::class.java)
	
	init {
		Disposer.register(this, ddsEditor)
	}
}