package icu.windea.pls.script.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

/**
 * 用于补充当前声明。
 */
class ParadoxScriptSmartEnterProcessor: SmartEnterProcessorWithFixers() {
	init {
		addFixers(EqualSignFixer())
		addEnterProcessors(MyFixEnterProcessor())
	}
	
	/**
	 * 补充等号。
	 */
	class EqualSignFixer: Fixer<ParadoxScriptSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: ParadoxScriptSmartEnterProcessor, element: PsiElement) {
			//要求光标位于行尾（忽略空白），且位于单独的（在文件顶部或者块中）字符串或者封装变量引用的末尾（忽略空白）
			val caretOffset = editor.caretModel.offset
			if(!editor.document.isAtLineEnd(caretOffset, true)) return
			val targetElement = element
				.parentOfTypes(ParadoxScriptString::class, ParadoxScriptScriptedVariableReference::class, withSelf = false)
				?.takeIf { it.isBlockValue() }
				?: return
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
	
	class MyFixEnterProcessor: FixEnterProcessor() {
		override fun doEnter(atCaret: PsiElement?, file: PsiFile?, editor: Editor, modified: Boolean): Boolean {
			plainEnter(editor)
			return true
		}
	}
}