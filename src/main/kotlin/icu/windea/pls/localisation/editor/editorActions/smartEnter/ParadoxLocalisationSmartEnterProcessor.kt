package icu.windea.pls.localisation.editor.editorActions.smartEnter

import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * 用于补充当前声明。
 */
class ParadoxLocalisationSmartEnterProcessor: SmartEnterProcessorWithFixers() {
	init {
		addFixers(AfterLocalisationKeyFixer())
	}
	
	class AfterLocalisationKeyFixer: Fixer<ParadoxLocalisationSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: ParadoxLocalisationSmartEnterProcessor, element: PsiElement) {
			//要求光标位于行尾（忽略空白），且位于属性名（propertyKey）的末尾（忽略空白）
			val caretOffset = editor.caretModel.offset
			if(!editor.document.isAtLineEnd(caretOffset, true)) return
			val targetElement = element
				.parent.castOrNull<ParadoxLocalisationPropertyKey>()
				//.parentOfType<ParadoxLocalisationPropertyKey>()
				?: return
			val endOffset = element.endOffset
			if(caretOffset != endOffset){
				editor.document.deleteString(caretOffset, endOffset)
			}
			val property = targetElement.parent as? ParadoxLocalisationProperty ?: return
			val category = ParadoxLocalisationCategory.resolve(property)
			val text = when(category) {
				ParadoxLocalisationCategory.Localisation -> ":0 \"\""
				ParadoxLocalisationCategory.SyncedLocalisation -> ": \"\""
				null -> ": \"\""
			}
			EditorModificationUtil.insertStringAtCaret(editor, text, false, text.length - 1)
		}
	}
}