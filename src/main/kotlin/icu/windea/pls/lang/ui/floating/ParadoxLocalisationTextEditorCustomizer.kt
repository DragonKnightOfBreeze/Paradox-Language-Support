package icu.windea.pls.lang.ui.floating

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorCustomizer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.coroutines.childScope
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

// NOTE the EP interface is internal (and not ignored by verifier since IDEA-262)
// com.intellij.openapi.fileEditor.impl.text.TextEditorCustomizer
// com.intellij.ui.codeFloatingToolbar.FloatingCodeToolbarEditorCustomizer
// org.intellij.plugins.markdown.ui.floating.AddFloatingToolbarTextEditorCustomizer

@Suppress("UnstableApiUsage")
class ParadoxLocalisationTextEditorCustomizer : TextEditorCustomizer {
    // NOTE 3.0.0 [compatibility] `TextEditorCustomizer.execute(TextEditor)` is remove since IDEA-262
    //  - Use `TextEditorCustomizer.execute(TextEditor, CoroutineScope)` instead

    override fun customize(textEditor: TextEditor, coroutineScope: CoroutineScope) {
        if (!shouldAcceptEditor(textEditor) || !shouldShowFloatingToolbar()) return

        // See ParadoxLocalisationFloatingToolbarCustomizableGroupProvider: the toolbar holds its scope until disposed.
        val toolbarScope = coroutineScope.childScope("ParadoxLocalisationTextEditorCustomizer")
        var registered = false
        try {
            val toolbar = ParadoxLocalisationFloatingToolbar(textEditor.editor,  toolbarScope)
            registered = Disposer.tryRegister(textEditor, toolbar)
            if (!registered) {
                Disposer.dispose(toolbar)
            }
        } finally {
            if (!registered) {
                toolbarScope.cancel()
            }
        }

        val toolbar = ParadoxLocalisationFloatingToolbar(textEditor.editor, coroutineScope)
        Disposer.register(textEditor, toolbar)
    }

    private fun shouldAcceptEditor(editor: TextEditor): Boolean {
        val file = editor.file
        return file.fileType is ParadoxLocalisationFileType || shouldAcceptScratchFile(editor)
    }

    private fun shouldAcceptScratchFile(editor: TextEditor): Boolean {
        val file = editor.file
        val project = editor.editor.project ?: return false
        return isScratchFile(file, project)
    }

    private fun isScratchFile(file: VirtualFile, project: Project): Boolean {
        return ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file, file.fileType) is ParadoxLocalisationLanguage
    }

    private fun shouldShowFloatingToolbar(): Boolean {
        return ChronicleSettings.getInstance().state.others.showLocalisationFloatingToolbar
    }
}
