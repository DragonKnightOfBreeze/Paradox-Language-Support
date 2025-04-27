package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.scratch.*
import com.intellij.lang.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import kotlinx.coroutines.*

//org.intellij.plugins.markdown.ui.floating.AddFloatingToolbarTextEditorCustomizer

//com.intellij.openapi.fileEditor.impl.text.TextEditorCustomizer
//NOTE the EP interface is marked as @Internal (nothing to say...)

@Suppress("UnstableApiUsage")
class ParadoxLocalisationTextEditorCustomizer : TextEditorCustomizer {
    override suspend fun execute(textEditor: TextEditor) {
        if (shouldAcceptEditor(textEditor) && shouldShowFloatingToolbar()) {
            coroutineScope {
                val toolbar = ParadoxLocalisationFloatingToolbar(textEditor.editor, this)
                Disposer.register(textEditor, toolbar)
            }
        }
    }

    private fun shouldAcceptEditor(editor: TextEditor): Boolean {
        val file = editor.file
        return file.fileType is ParadoxLocalisationFileType || shouldAcceptScratchFile(editor)
    }

    private fun shouldAcceptScratchFile(editor: TextEditor): Boolean {
        val file = editor.file
        val project = editor.editor.project ?: return false
        return isParadoxScratchFile(file, project)
    }

    private fun shouldShowFloatingToolbar(): Boolean {
        return getSettings().others.showLocalisationFloatingToolbar
    }

    private fun isParadoxScratchFile(file: VirtualFile, project: Project): Boolean {
        return ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file, file.fileType) is ParadoxLocalisationLanguage
    }
}
