package icu.windea.pls.localisation.ui.preview

import com.intellij.ide.scratch.*
import com.intellij.lang.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.advanced.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.ui.floating.*

//org.intellij.plugins.markdown.ui.preview.MarkdownTextEditorProvider

/**
 * @see icu.windea.pls.localisation.ui.floating.FloatingToolbar
 */
class ParadoxLocalisationTextEditorProvider: PsiAwareTextEditorProvider() {
	override fun accept(project: Project, file: VirtualFile): Boolean {
		if (!super.accept(project, file)) {
			return false
		}
		return FileTypeRegistry.getInstance().isFileOfType(file, ParadoxLocalisationFileType) || shouldAcceptScratchFile(project, file)
	}
	
	private fun shouldAcceptScratchFile(project: Project, file: VirtualFile): Boolean {
		return ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file, file.fileType) == ParadoxLocalisationLanguage
	}
	
	override fun createEditor(project: Project, file: VirtualFile): FileEditor {
		val actualEditor = super.createEditor(project, file)
		if (actualEditor is TextEditor && !AdvancedSettings.getBoolean("paradoxLocalisation.hide.floating.toolbar")) {
			val toolbar = FloatingToolbar(actualEditor)
			Disposer.register(actualEditor, toolbar)
		}
		return actualEditor
	}
	
	@Suppress("UnstableApiUsage")
	override fun getPolicy(): FileEditorPolicy {
		return FileEditorPolicy.HIDE_OTHER_EDITORS
	}
}