package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.TemplateBuilderFactory.*
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.icons.AllIcons
import com.intellij.openapi.command.impl.FinishMarkAction.*
import com.intellij.openapi.command.impl.StartMarkAction.*
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.buildInlineTemplate
import icu.windea.pls.core.codeInsight.TemplateEditingFinishedListener
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.core.icon
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.cwt.formatter.CwtCodeStyleSettings
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.type.CwtExpressionType
import javax.swing.Icon

object CwtCompletionLookupProvider {
    // region Constants

    private val LOOKUP_ELEMENT_YES = LookupElementBuilder.create(ChronicleStrings.yesKeyword).bold()
        .withPriority(CwtCompletionPriorities.keyword)
        .withCompletionId()
    private val LOOKUP_ELEMENT_NO = LookupElementBuilder.create(ChronicleStrings.noKeyword).bold()
        .withPriority(CwtCompletionPriorities.keyword)
        .withCompletionId()
    private val LOOKUP_ELEMENT_BLOCK = LookupElementBuilder.create("").withPresentableText(ChronicleStrings.blockKeyword)
        .withPriority(CwtCompletionPriorities.keyword)
        .withCompletionId(ChronicleStrings.blockKeyword)
        .withInsertHandler(BlockInsertHandler())
    private val LOOKUP_ELEMENT_KEYWORD = listOf(LOOKUP_ELEMENT_YES, LOOKUP_ELEMENT_NO, LOOKUP_ELEMENT_BLOCK)
    private val LOOKUP_ELEMENT_BOOL = listOf(LOOKUP_ELEMENT_YES, LOOKUP_ELEMENT_NO)
    private val LOOKUP_ELEMENT_CARDINALITY = listOf("0..1", "1..1", "0..inf", "1..inf").map {
        LookupElementBuilder.create(it)
            .withPriority(CwtCompletionPriorities.constant)
            .withCompletionId()
    }

    // endregion

    // region Providers (keywords)

    @Suppress("unused")
    fun forYesKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_YES
    @Suppress("unused")
    fun forNoKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_NO
    fun forBlockKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_BLOCK
    fun forKeyword(): List<LookupElementBuilder> = LOOKUP_ELEMENT_KEYWORD
    fun forBool(): List<LookupElementBuilder> = LOOKUP_ELEMENT_BOOL
    fun forCardinality(): List<LookupElementBuilder> = LOOKUP_ELEMENT_CARDINALITY

    // endregion

    // region Providers (schema)

    fun forSchemaConstant(lookupString: String, element: PsiElement? = null, typeFile: PsiFile? = null, icon: Icon? = null, hintText: String? = null): LookupElementBuilder {
        return LookupElementBuilder.create(lookupString).withPsiElement(element)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withIcon(icon)
            .withPatchableTailText(hintText)
            .withPriority(CwtCompletionPriorities.constant)
    }

    fun forSchemaEnumValue(lookupString: String, element: PsiElement? = null, typeFile: PsiFile? = null, icon: Icon? = null, hintText: String? = null): LookupElementBuilder {
        return LookupElementBuilder.create(lookupString).withPsiElement(element)
            .withIcon(icon)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableTailText(hintText)
            .withPriority(CwtCompletionPriorities.enumName)
    }

    fun forSchemaTemplate(lookupString: String, element: PsiElement? = null, typeFile: PsiFile? = null, icon: Icon? = null, hintText: String? = null): LookupElementBuilder {
        return LookupElementBuilder.create(lookupString).withPsiElement(element)
            .withIcon(icon)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableTailText(hintText)
    }

    fun forSchemaTemplateEnum(lookupString: String, element: PsiElement? = null, typeFile: PsiFile? = null, hintText: String? = null): LookupElementBuilder {
        return LookupElementBuilder.create(lookupString).withPsiElement(element)
            .withIcon(AllIcons.Nodes.Enum)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withTailText(hintText, true)
    }

    fun forSchemaTemplateParameter(lookupString: String, element: PsiElement? = null, hintText: String? = null): LookupElementBuilder {
        return LookupElementBuilder.create(lookupString).withPsiElement(element)
            .withIcon(AllIcons.Nodes.Parameter)
            .withTailText(hintText, true)
    }

    // endregion

    // region Wrappers

    fun wrapForConfig(lookupElement: LookupElementBuilder, context: CwtConfigCompletionContext, config: CwtConfig<*>, schemaExpression: CwtSchemaExpression): LookupElement? {
        if (lookupElement in forKeyword()) return lookupElement

        val isKeyConfig = config is CwtOptionConfig || config is CwtPropertyConfig

        val lookupString = when {
            context.leftQuoted -> lookupElement.lookupString // already quoted
            else -> lookupElement.lookupString.quoteIfNeeded() // #369 should be quoted if is blank or contains blank
        }
        val insertCurlyBraces = when {
            config is CwtOptionMemberConfig<*> -> config.valueType == CwtExpressionType.Block
            config is CwtMemberConfig<*> -> config.valueType == CwtExpressionType.Block
            else -> return null
        }
        val valueText = when {
            insertCurlyBraces -> ChronicleStrings.blockFolder
            config is CwtOptionMemberConfig<*> -> config.value
            config is CwtMemberConfig<*> -> config.value
            else -> return null
        }
        val withValueText = when {
            isKeyConfig && !context.isKeyOnly && !context.isValueOnly -> " = $valueText"
            else -> ""
        }

        // 排除重复项
        val completionId = lookupString + withValueText
        if (!context.completionIds.add(completionId)) return null

        var result = lookupElement

        result = result.withBaseLookupString(lookupString) // #369
        result = result.patchIcon()
        result = result.patchTailText(withValueText)

        if (context.isKeyOnly || context.isValueOnly) { // key or value only
            result = result.withInsertHandler(KeyOrValueOnlyInsertHandler(context))
        } else if (isKeyConfig && context.isKey) { // key with value
            result = result.withInsertHandler(KeyWithValueInsertHandler(context, insertCurlyBraces))
        }

        if (schemaExpression is CwtSchemaExpression.Template) {
            result = result.withInsertHandler(TemplateInsertHandler(context, schemaExpression, result.insertHandler))
        }

        return result
    }

    private fun LookupElementBuilder.patchIcon(): LookupElementBuilder {
        val patchableIcon = patchableIcon
        if (patchableIcon == null) return this
        return withIcon(patchableIcon)
    }

    private fun LookupElementBuilder.patchTailText(withValueText: String): LookupElementBuilder {
        val patchableTailText = patchableTailText
        val patchedTailText = getPatchedTailText(withValueText, patchableTailText)
        if (patchedTailText.isEmpty()) return this
        return withTailText(patchedTailText, true)
    }

    private fun getPatchedTailText(withValueText: String, patchableTailText: String?): String = buildString {
        append(withValueText)
        if (patchableTailText != null) append(patchableTailText)
    }

    // endregion

    // region Insert Handlers

    private open class BlockInsertHandler<T : LookupElement> : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            // 插入成对的花括号
            val codeStyleSettings = CwtCodeStyleSettings.getInstance(c.file)
            val spaceWithinBraces = codeStyleSettings.SPACE_WITHIN_BRACES
            val text = if (spaceWithinBraces) "{  }" else "{}"
            val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(c.editor, text, false, true, length)
        }
    }

    private open class KeyOrValueOnlyInsertHandler<T : LookupElement>(
        private val context: CwtConfigCompletionContext,
    ) : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            // `isKey` 如果是 `null`，则表示已经填充的只是键或值的其中一部分
            if (!context.leftQuoted) return
            val editor = c.editor
            val caretOffset = editor.caretModel.offset
            val charsSequence = editor.document.charsSequence
            val rightQuoted = charsSequence.get(caretOffset) == '"' && charsSequence.get(caretOffset - 1) != '\\'
            if (rightQuoted) {
                // 在必要时将光标移到右双引号之后
                editor.caretModel.moveToOffset(caretOffset + 1)
            } else {
                // 插入缺失的右双引号，且在必要时将光标移到右双引号之后
                EditorModificationUtil.insertStringAtCaret(editor, "\"", false, true)
            }
        }
    }

    private open class KeyWithValueInsertHandler<T : LookupElement>(
        context: CwtConfigCompletionContext,
        private val insertCurlyBraces: Boolean,
    ) : KeyOrValueOnlyInsertHandler<T>(context) {
        override fun handleInsert(c: InsertionContext, item: T) {
            // call super first
            super.handleInsert(c, item)

            val editor = c.editor
            val codeStyleSettings = CwtCodeStyleSettings.getInstance(c.file)
            val spaceAroundPropertySeparator = codeStyleSettings.SPACE_AROUND_PROPERTY_SEPARATOR
            val spaceWithinBraces = codeStyleSettings.SPACE_WITHIN_BRACES
            val text = buildString {
                if (spaceAroundPropertySeparator) append(" ")
                append("=")
                if (spaceAroundPropertySeparator) append(" ")
                if (insertCurlyBraces) {
                    if (spaceWithinBraces) append("{  }") else append("{}")
                }
            }
            val length = if (insertCurlyBraces) {
                if (spaceWithinBraces) text.length - 2 else text.length - 1
            } else {
                text.length
            }
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
    }

    private open class TemplateInsertHandler<T : LookupElement>(
        private val context: CwtConfigCompletionContext,
        private val schemaExpression: CwtSchemaExpression.Template,
        private val oldInsertHandler: InsertHandler<LookupElement>?,
    ) : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            val caretOffset1 = c.editor.caretModel.offset
            oldInsertHandler?.handleInsert(c, item)
            val caretOffset2 = c.editor.caretModel.offset
            val caretMarker = c.editor.document.createRangeMarker(caretOffset1, caretOffset2)
            caretMarker.isGreedyToRight = true
            c.editor.caretModel.moveToOffset(caretMarker.startOffset)
            c.laterRunnable = Runnable {
                val project = c.project
                val editor = c.editor
                val commandName = ChronicleBundle.message("command.expandTemplate.name")
                executeWriteCommand(project, commandName, makeWritable = c.file) c@{
                    val documentManager = PsiDocumentManager.getInstance(project)
                    documentManager.commitDocument(editor.document)
                    val elementOffset = caretMarker.startOffset - 1
                    val element = c.file.findElementAt(elementOffset)?.parent
                    if (element !is CwtPropertyKey && element !is CwtString) return@c
                    val startAction = start(editor, project, commandName)
                    val templateBuilder = getInstance().createTemplateBuilder(element)
                    val shift = element.startOffset + if (context.leftQuoted) 1 else 0
                    schemaExpression.parameterRanges.forEach { parameterRange ->
                        val parameterText = parameterRange.substring(schemaExpression.expressionString)
                        val expression = CwtConfigCompletionTemplateExpression.resolve(context, parameterRange, parameterText)
                            ?: TextExpression(parameterText)
                        templateBuilder.replaceRange(parameterRange.shiftRight(shift), expression)
                    }
                    val textRange = element.textRange
                    editor.caretModel.moveToOffset(textRange.startOffset)
                    val template = templateBuilder.buildInlineTemplate()
                    TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
                        c.editor.caretModel.moveToOffset(caretMarker.endOffset)
                        finish(project, editor, startAction)
                    })
                }
            }
        }
    }

    // endregion
}
