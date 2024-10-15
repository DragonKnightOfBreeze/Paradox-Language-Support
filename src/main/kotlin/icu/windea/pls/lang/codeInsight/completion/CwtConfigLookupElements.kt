package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import icu.windea.pls.cwt.codeStyle.*

object CwtConfigLookupElements {
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText("{...}")
        .withInsertHandler { c, _ ->
            val editor = c.editor
            val customSettings = CodeStyle.getCustomSettings(c.file, CwtCodeStyleSettings::class.java)
            val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
            val text = if (spaceWithinBraces) "{  }" else "{}"
            val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
}
