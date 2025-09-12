package icu.windea.pls.localisation.editor.editorActions.smartEnter

import com.intellij.lang.SmartEnterProcessorWithFixers
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 用于补充当前声明。
 */
class ParadoxLocalisationSmartEnterProcessor : SmartEnterProcessorWithFixers() {
    init {
        addFixers(AfterLocalisationKeyFixer())
    }

    class AfterLocalisationKeyFixer : Fixer<ParadoxLocalisationSmartEnterProcessor>() {
        override fun apply(editor: Editor, processor: ParadoxLocalisationSmartEnterProcessor, element: PsiElement) {
            // 要求光标位于行尾（忽略空白），且位于属性名（propertyKey）的末尾（忽略空白）
            val offset = editor.caretModel.offset
            val document = editor.document
            val lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset))
            val s = document.immutableCharSequence.subSequence(offset, lineEndOffset)
            if (s.isNotBlank()) return
            val targetElement = element.parent.castOrNull<ParadoxLocalisationPropertyKey>() ?: return
            val endOffset = element.endOffset
            if (offset != endOffset) {
                document.deleteString(offset, endOffset)
            }
            val property = targetElement.parent as? ParadoxLocalisationProperty ?: return
            val type = ParadoxLocalisationType.resolve(property)
            val text = when (type) {
                ParadoxLocalisationType.Normal -> ":0 \"\""
                ParadoxLocalisationType.Synced -> ": \"\""
                null -> ": \"\""
            }
            EditorModificationUtil.insertStringAtCaret(editor, text, false, text.length - 1)
        }
    }
}
