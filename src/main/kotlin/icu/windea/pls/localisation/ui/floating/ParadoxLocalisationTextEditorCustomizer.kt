package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.scratch.*
import com.intellij.lang.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.*
import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*

//org.intellij.plugins.markdown.ui.floating.AddFloatingToolbarTextEditorCustomizer

class ParadoxLocalisationTextEditorCustomizer : TextEditorCustomizer {
    override fun customize(textEditor: TextEditor) {
        if (shouldAcceptEditor(textEditor) && shouldShowFloatingToolbar()) {
            val toolbar = ParadoxLocalisationFloatingToolbar(textEditor, "Pls.ParadoxLocalisation.Toolbar.Floating")
            Disposer.register(textEditor, toolbar)
        }
    }

    private fun shouldAcceptEditor(editor: TextEditor): Boolean {
        val file = editor.file
        return file.fileType == ParadoxLocalisationFileType || shouldAcceptScratchFile(editor)
    }

    private fun shouldAcceptScratchFile(editor: TextEditor): Boolean {
        val file = editor.file
        val project = editor.editor.project ?: return false
        return ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file, file.fileType) == ParadoxLocalisationLanguage
    }

    private fun shouldShowFloatingToolbar(): Boolean {
        return getSettings().others.showLocalisationFloatingToolbar
    }
}
