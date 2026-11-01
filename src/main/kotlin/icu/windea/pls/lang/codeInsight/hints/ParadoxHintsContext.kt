package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.codeInsight.hints.HintsContext

class ParadoxHintsContext(
    file: PsiFile,
    editor: Editor,
    val settings: ParadoxHintsSettings,
    val factory: PresentationFactory,
) : HintsContext(file, editor)
