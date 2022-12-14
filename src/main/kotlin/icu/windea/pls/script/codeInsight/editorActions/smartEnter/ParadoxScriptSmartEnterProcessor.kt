package icu.windea.pls.script.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

/**
 * 用于补充当前声明。
 */
class ParadoxScriptSmartEnterProcessor: SmartEnterProcessorWithFixers() {
	init {
		addFixers(
			EqualSignFixer()
		)
	}
	
	/**
	 * 补充等号。
	 */
	class EqualSignFixer: Fixer<ParadoxScriptSmartEnterProcessor>() {
		override fun apply(editor: Editor, processor: ParadoxScriptSmartEnterProcessor, element: PsiElement) {
			//要求位于单独的（在文件顶部或者块中）字符串或者封装变量引用的末尾
			if(editor.caretModel.offset != element.textRange.endOffset) return
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
}