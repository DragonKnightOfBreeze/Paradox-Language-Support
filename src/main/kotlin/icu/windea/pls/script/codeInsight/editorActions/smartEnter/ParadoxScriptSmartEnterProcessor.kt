package icu.windea.pls.script.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

/**
 * 用于补充当前声明。
 */
class ParadoxScriptSmartEnterProcessor: SmartEnterProcessorWithFixers() {
	init {
		addFixers(
			EqualSignFixer(),
			MissingRightCurlyBraceFixer()
		)
	}
	
	/**
	 * 补充等号。
	 */
	class EqualSignFixer: Fixer<ParadoxScriptSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: ParadoxScriptSmartEnterProcessor, element: PsiElement) {
			//光标位于行尾（忽略空白），并且这一行以单独的字符串或者封装变量引用（在顶级或者块中的）结束
			val offset = editor.caretModel.offset
			val document = editor.document
			if(!document.isAtLineEnd(offset, true)) return
			val targetElement = element.siblings(forward = false, withSelf = false)
				.find { it !is PsiWhiteSpace && it.textLength != 0 }
				?.parentOfTypes(ParadoxScriptString::class, ParadoxScriptScriptedVariableReference::class, withSelf = false)
				?.takeIf { it.isBlockValue() }
				?: return
			val index = targetElement.textRange.endOffset
			val lineEndOffset = DocumentUtil.getLineEndOffset(offset, document)
			if(index + 1 != lineEndOffset) {
				document.deleteString(index + 1, lineEndOffset)
			}
			val customSettings = CodeStyle.getCustomSettings(element.containingFile, ParadoxScriptCodeStyleSettings::class.java)
			val spaceAroundSeparator = when(targetElement) {
				is ParadoxScriptString -> customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
				is ParadoxScriptScriptedVariableReference -> customSettings.SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR
				else -> return
			}
			if(spaceAroundSeparator) {
				EditorModificationUtil.insertStringAtCaret(editor, " = ")
			} else {
				EditorModificationUtil.insertStringAtCaret(editor, "=")
			}
		}
	}
	
	/**
	 * 补充缺失的右花括号。
	 */
	class MissingRightCurlyBraceFixer: Fixer<ParadoxScriptSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: ParadoxScriptSmartEnterProcessor, element: PsiElement) {
			//光标位于行尾（忽略空白），并且这一行以左花括号结束
			val offset = editor.caretModel.offset
			val document = editor.document
			if(!document.isAtLineEnd(offset, true)) return
			val targetElement = element.siblings(forward = false, withSelf = false)
				.find { it !is PsiWhiteSpace && it.textLength != 0 }
				?: return
			if(targetElement.elementType != ParadoxScriptElementTypes.LEFT_BRACE) return
			val index = targetElement.textRange.endOffset
			val lineEndOffset = DocumentUtil.getLineEndOffset(offset, document)
			if(index + 1 != lineEndOffset) {
				document.deleteString(index + 1, lineEndOffset)
			}
			val customSettings = CodeStyle.getCustomSettings(element.containingFile, ParadoxScriptCodeStyleSettings::class.java)
			val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
			if(spaceWithinBraces) {
				EditorModificationUtil.insertStringAtCaret(editor, "  }", false, 1)
			} else {
				EditorModificationUtil.insertStringAtCaret(editor, "}", false, 0)
			}
		}
	}
}