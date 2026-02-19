package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.TemplateBuilder
import com.intellij.codeInsight.template.TemplateBuilderFactory
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.command.impl.FinishMarkAction
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.buildInlineTemplate
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.TemplateEditingFinishedListener
import icu.windea.pls.core.executeWriteCommand
import icu.windea.pls.core.processChild
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.lang.codeStyle.PlsCodeStyleUtil
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.ui.clause.ElementDescriptor
import icu.windea.pls.lang.ui.clause.ElementsContext
import icu.windea.pls.lang.ui.clause.ElementsInfo
import icu.windea.pls.lang.ui.clause.ExpandClauseTemplateDialog
import icu.windea.pls.lang.ui.clause.PropertyDescriptor
import icu.windea.pls.lang.ui.clause.ValueDescriptor
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

@Suppress("UnstableApiUsage")
object ParadoxClauseTemplateCompletionManager {
    private const val blockFolderText = "{ <generate via template> }"
    private const val blockFolderId = "{$...$}"

    fun buildLookupElement(context: ProcessingContext, config: CwtConfig<*>, lookupElement: LookupElementBuilder): LookupElementBuilder? {
        if (!PlsSettings.getInstance().state.completion.completeWithClauseTemplate) return null

        val entryConfigs = CwtConfigManager.getEntryConfigs(config)
        val insertHandler = getExpandInsertHandler(context, entryConfigs)
        if (insertHandler == null) return null
        val extraTailText = buildString {
            append(" = ").append(blockFolderText)
            if (lookupElement.patchableTailText != null) append(lookupElement.patchableTailText)
        }
        val result = lookupElement
            .withTailText(extraTailText, true)
            .withInsertHandler(insertHandler)
            .withCompletionId(lookupElement.lookupString + " = " + blockFolderId)
            .withPriority(lookupElement.priority)
        return result
    }

    fun buildBlockLookupElement(context: ProcessingContext, config: CwtConfig<*>): LookupElementBuilder? {
        if (!PlsSettings.getInstance().state.completion.completeWithClauseTemplate) return null

        val entryConfigs = CwtConfigManager.getEntryConfigs(config)
        val insertHandler = getExpandInsertHandler(context, entryConfigs) ?: return null
        val extraTailText = blockFolderText
        val result = LookupElementBuilder.create("")
            .withPresentableText(extraTailText)
            .withInsertHandler(insertHandler)
            .withCompletionId(blockFolderId)
            .withPriority(PlsCompletionPriorities.keyword)
        return result
    }

    fun getExpandInsertHandler(context: ProcessingContext, entryConfigs: List<CwtMemberConfig<*>>): InsertHandler<LookupElement>? {
        // 如果补全位置所在的子句为空或者都不精确匹配，显示对话框时默认列出的属性/值应该有数种情况，因此这里需要传入entryConfigs
        // 默认列出且仅允许选择直接的key为常量字符串的属性（忽略需要内联的情况）

        val file = context.parameters?.originalFile ?: return null
        val constantConfigGroupList = mutableListOf<Map<CwtDataExpression, List<CwtMemberConfig<*>>>>()
        val hasRemainList = mutableListOf<Boolean>()
        for (entry in entryConfigs) {
            val constantConfigGroup = entry.configs
                ?.filter { it is CwtPropertyConfig && it.configExpression.type == CwtDataTypes.Constant }
                ?.groupBy { it.configExpression }
                .orEmpty()
            if (constantConfigGroup.isEmpty()) continue // skip
            val configList = entry.configs
                ?.distinctBy { it.configExpression }
                .orEmpty()
            val hasRemain = constantConfigGroup.size != configList.size
            constantConfigGroupList.add(constantConfigGroup)
            hasRemainList.add(hasRemain)
        }
        if (constantConfigGroupList.isEmpty()) return null
        val config = context.config!!
        val propertyName = CwtConfigManager.getEntryName(config)

        val params = PlsInsertHandlers.Params(quoted = context.quoted, isKey = context.isKey, insertCurlyBraces = true)

        return InsertHandler { c, _ ->
            if (params.isKey == true) {
                PlsInsertHandlers.applyKeyWithValue(c, params)
            } else {
                PlsInsertHandlers.applyBlock(c)
            }

            c.laterRunnable = Runnable {
                val project = file.project
                val editor = c.editor
                val descriptorsInfoList = constantConfigGroupList.indices.map { i ->
                    val descriptors = getDescriptors(constantConfigGroupList[i])
                    val hasRemain = hasRemainList[i]
                    ElementsInfo(descriptors, hasRemain)
                }
                val descriptorsContext = ElementsContext(project, editor, propertyName, descriptorsInfoList)

                val dialog = ExpandClauseTemplateDialog(project, editor, descriptorsContext)
                if (!dialog.showAndGet()) return@Runnable

                val descriptors = descriptorsContext.descriptorsInfo.resultDescriptors
                val hasRemain = descriptorsContext.descriptorsInfo.hasRemain

                val multiline = descriptors.size > PlsSettings.getInstance().state.completion.clauseTemplate.maxMemberCountInOneLine
                val around = PlsCodeStyleUtil.isSpaceAroundPropertySeparator(file)

                val commandName = PlsBundle.message("script.command.expandClauseTemplate.name")
                executeWriteCommand(project, commandName, makeWritable = file) {
                    val documentManager = PsiDocumentManager.getInstance(project)
                    documentManager.commitDocument(editor.document)
                    val caretOffset = editor.caretModel.offset
                    val elementOffset = if (around) caretOffset + 1 else caretOffset
                    val elementAtCaret = file.findElementAt(elementOffset)?.parent as ParadoxScriptValue
                    val clauseText = buildClauseText(descriptors, multiline, around)
                    val clauseElement = ParadoxScriptElementFactory.createValue(project, clauseText)
                    val element = elementAtCaret.replace(clauseElement) as ParadoxScriptBlock
                    documentManager.doPostponedOperationsAndUnblockDocument(editor.document) // 提交文档更改

                    val startAction = StartMarkAction.start(editor, project, commandName)
                    val templateBuilder = TemplateBuilderFactory.getInstance().createTemplateBuilder(element)
                    processTemplateBuilder(templateBuilder, descriptors, element)
                    val textRange = element.textRange
                    val caretMarker = editor.document.createRangeMarker(textRange.startOffset, textRange.endOffset)
                    caretMarker.isGreedyToRight = true
                    editor.caretModel.moveToOffset(textRange.startOffset)
                    val template = templateBuilder.buildInlineTemplate()
                    TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
                        try {
                            // 如果从句中没有其他可能的元素，将光标移到子句的末尾
                            if (!hasRemain) editor.caretModel.moveToOffset(caretMarker.endOffset)
                            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
                        } finally {
                            FinishMarkAction.finish(project, editor, startAction)
                        }
                    })
                }
            }
        }
    }

    private fun getDescriptors(constantConfigGroup: Map<CwtDataExpression, List<CwtMemberConfig<*>>>): List<ElementDescriptor> {
        val descriptors = mutableListOf<ElementDescriptor>()
        for ((expression, constantConfigs) in constantConfigGroup) {
            if (expression.isKey) {
                val name = expression.expressionString
                val constantValueExpressions = constantConfigs
                    .mapNotNull { it.castOrNull<CwtPropertyConfig>()?.valueExpression?.takeIf { e -> e.type == CwtDataTypes.Constant } }
                val mustBeConstantValue = constantValueExpressions.size == constantConfigs.size
                val value = if (mustBeConstantValue) constantValueExpressions.first().expressionString else ""
                val constantValues = if (constantValueExpressions.isEmpty()) emptyList() else buildList {
                    if (!mustBeConstantValue) add("")
                    constantValueExpressions.forEach { add(it.expressionString) }
                }
                val descriptor = PropertyDescriptor(name = name, value = value, constantValues = constantValues)
                descriptors.add(descriptor)
            } else {
                val descriptor = ValueDescriptor(name = expression.expressionString)
                descriptors.add(descriptor)
            }
        }
        return descriptors
    }

    private fun buildClauseText(descriptors: MutableList<ElementDescriptor>, multiline: Boolean, around: Boolean): String {
        return buildString {
            this.append("{")
            if (multiline) this.append("\n")
            descriptors.forEach {
                when (it) {
                    is ValueDescriptor -> {
                        this.append(it.name.quoteIfNecessary())
                    }
                    is PropertyDescriptor -> {
                        this.append(it.name.quoteIfNecessary())
                        if (around) this.append(" ")
                        append(it.separator)
                        if (around) this.append(" ")
                        this.append(it.value.ifEmpty { "v" })
                    }
                }
                if (multiline) this.append("\n") else this.append(" ")
            }
            this.append("}")
        }
    }

    private fun processTemplateBuilder(templateBuilder: TemplateBuilder, descriptors: MutableList<ElementDescriptor>, element: ParadoxScriptBlock) {
        var i = 0
        element.processChild { e ->
            if (e is ParadoxScriptProperty || e is ParadoxScriptValue) {
                val descriptor = descriptors[i]
                if (descriptor.editInTemplate) {
                    if (e is ParadoxScriptProperty && descriptor is PropertyDescriptor) {
                        val string = if (descriptor.value.isNotEmpty()) descriptor.value.quoteIfNecessary() else ""
                        val expression = TextExpression(string)
                        templateBuilder.replaceElement(e.propertyValue!!, "${descriptor.name}_$i", expression, true)
                    }
                }
                i++
            }
            true
        }
    }
}
