package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorCustomizer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import kotlinx.coroutines.coroutineScope

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
        return PlsFacade.getSettings().others.showLocalisationFloatingToolbar
    }

    private fun isParadoxScratchFile(file: VirtualFile, project: Project): Boolean {
        return ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file, file.fileType) is ParadoxLocalisationLanguage
    }
}
