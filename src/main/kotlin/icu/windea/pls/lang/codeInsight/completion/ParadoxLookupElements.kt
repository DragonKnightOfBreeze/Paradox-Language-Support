package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import icu.windea.pls.script.codeStyle.*

object ParadoxLookupElements {
	val yesLookupElement = LookupElementBuilder.create("yes").bold().withPriority(ParadoxCompletionPriorities.keywordPriority)
	val noLookupElement = LookupElementBuilder.create("no").bold().withPriority(ParadoxCompletionPriorities.keywordPriority)
	val blockLookupElement = LookupElementBuilder.create("")
		.withPresentableText("{...}")
		.withInsertHandler { c, _ ->
			val editor = c.editor
			val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
			val text = if(customSettings.SPACE_WITHIN_BRACES) "{  }" else "{}"
			val length = if(customSettings.SPACE_WITHIN_BRACES) text.length - 2 else text.length - 1
			EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
		}
		.withPriority(ParadoxCompletionPriorities.keywordPriority)
}