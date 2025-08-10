package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.ui.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供语言区域名字的代码补全。
 */
class ParadoxLocalisationLocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
    //如果之后没有英文冒号，则插入英文冒号（如果之后没有更多行，则还要插入换行符和必要的缩进），否则光标移到冒号之后
    private val insertHandler = InsertHandler<LookupElement> { context, _ ->
        val editor = context.editor
        val chars = editor.document.charsSequence
        val colonIndex = chars.indexOf(':', context.startOffset)
        if (colonIndex != -1) {
            editor.caretModel.moveToOffset(colonIndex + 1)
        } else {
            val settings = CodeStyle.getSettings(context.file)
            val indentOptions = settings.getIndentOptions(ParadoxLocalisationFileType)
            val insertLineBreak = editor.document.getLineNumber(editor.caretModel.offset) == editor.document.lineCount - 1
            val s = buildString {
                append(":")
                if (insertLineBreak) {
                    append("\n")
                    repeat(indentOptions.INDENT_SIZE) { append(" ") }
                }
            }
            EditorModificationUtil.insertStringAtCaret(editor, s)
        }
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        if (!ParadoxPsiManager.isLocalisationLocaleLike(position)) return

        val file = parameters.originalFile
        val project = file.project
        val localeIdFromFileName = file.castOrNull<ParadoxLocalisationFile>()?.let { ParadoxLocalisationFileManager.getLocaleIdFromFileName(it) }
        //批量提示
        val lookupElements = mutableSetOf<LookupElement>()
        val locales = PlsFacade.getConfigGroup(project, null).localisationLocalesById.values
        for (locale in locales) {
            ProgressManager.checkCanceled()
            val element = locale.pointer.element ?: continue
            val typeFile = locale.pointer.containingFile
            val matched = localeIdFromFileName?.let { it == locale.id }
            val lookupElement = LookupElementBuilder.create(element, locale.id)
                .withIcon(PlsIcons.Nodes.LocalisationLocale)
                .withTailText(locale.text)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(insertHandler)
                .letIf(matched == false) {
                    it.withItemTextForeground(JBColor.GRAY) //将不匹配的语言区域的提示项置灰
                }
                .letIf(matched == true) {
                    it.withPriority(ParadoxCompletionPriorities.pinned) //优先提示与文件名匹配的语言区域
                }
            lookupElements.add(lookupElement)
        }
        result.addAllElements(lookupElements)
    }
}
