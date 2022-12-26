package icu.windea.pls.cwt.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.*

/**
 * 用于补充当前声明。
 */
class CwtSmartEnterProcessor: SmartEnterProcessorWithFixers() {
	init {
		addFixers(
			EqualSignFixer()
		)
	}
	
	class EqualSignFixer: Fixer<CwtSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: CwtSmartEnterProcessor, element: PsiElement) {
			//要求光标位于行尾（忽略空白），且位于单独的（在文件顶部或者块中，或者选项注释中的单独的）的末尾（忽略空白）
			val caretOffset = editor.caretModel.offset
			if(!editor.document.isAtLineEnd(caretOffset, true)) return
			val targetElement = element
				.parentOfTypes(CwtString::class)
				?.takeIf { it.parent is CwtBlockElement }
				//?.takeIf { it.isBlockValue() }
				?: return
			val endOffset = element.textRange.endOffset
			if(caretOffset != endOffset){
				editor.document.deleteString(endOffset, caretOffset)
			}
			val customSettings = CodeStyle.getCustomSettings(element.containingFile, CwtCodeStyleSettings::class.java)
			val spaceAroundSeparator = when {
				targetElement.isBlockValue() -> customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
				else -> customSettings.SPACE_AROUND_OPTION_SEPARATOR
			}
			if(spaceAroundSeparator) {
				EditorModificationUtil.insertStringAtCaret(editor, " = ")
			} else {
				EditorModificationUtil.insertStringAtCaret(editor, "=")
			}
		}
	}
}