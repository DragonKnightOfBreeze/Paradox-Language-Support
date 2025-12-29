package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class ParadoxHintsContext(
    val file: PsiFile,
    val editor: Editor,
    val settings: ParadoxHintsSettings,
    val factory: PresentationFactory,
) {
    val project = file.project
}
