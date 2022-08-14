package icu.windea.pls.localisation.ui.preview

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import org.jdom.*

//org.intellij.plugins.markdown.ui.split.SplitTextEditorProvider

//这是必要的，否则FloatingToolbar会不起效（实际使用的editor和被注册监听器的editor不是同一个），以后也可以考虑扩展

class ParadoxLocalisationEditorProvider : AsyncFileEditorProvider, DumbAware {
	val textEditorProvider = ParadoxLocalisationTextEditorProvider()
	
	override fun accept(project: Project, file: VirtualFile): Boolean {
		return textEditorProvider.accept(project, file)
	}
	
	override fun createEditor(project: Project, file: VirtualFile): FileEditor {
		return createEditorAsync(project, file).build()
	}
	
	override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
		val builder = getBuilderFromEditorProvider(textEditorProvider, project, file)
		return object : AsyncFileEditorProvider.Builder() {
			override fun build(): FileEditor {
				return builder.build()
			}
		}
	}
	
	fun getBuilderFromEditorProvider(provider: FileEditorProvider, project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
		return if(provider is AsyncFileEditorProvider) {
			provider.createEditorAsync(project, file)
		} else {
			object : AsyncFileEditorProvider.Builder() {
				override fun build(): FileEditor {
					return provider.createEditor(project, file)
				}
			}
		}
	}
	
	override fun readState(sourceElement: Element, project: Project, file: VirtualFile): FileEditorState {
		return textEditorProvider.readState(sourceElement, project, file)
	}
	
	override fun writeState(state: FileEditorState, project: Project, targetElement: Element) {
		return textEditorProvider.writeState(state, project, targetElement)
	}
	
	override fun getEditorTypeId(): String {
		return "paradox-localisation-provider"
	}
	
	override fun getPolicy(): FileEditorPolicy {
		return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
	}
}