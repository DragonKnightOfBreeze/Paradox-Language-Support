package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.command.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import javax.swing.*

fun CompletionResultSet.addElement(lookupElement: LookupElement?, context: ProcessingContext) {
    getFinalElement(lookupElement, context)?.let { addElement(it) }
    lookupElement?.extraLookupElements?.forEach { extraLookupElement ->
        getFinalElement(extraLookupElement, context)?.let { addElement(it) }
    }
}

private fun getFinalElement(lookupElement: LookupElement?, context: ProcessingContext): LookupElement? {
    if(lookupElement == null) return null
    val completionIds = context.completionIds
    if(completionIds?.let { ids -> lookupElement.completionId?.let { id -> ids.add(id) } } == false) return null
    val priority = lookupElement.priority
    if(priority != null) return PrioritizedLookupElement.withPriority(lookupElement, priority)
    return lookupElement
}

fun <T : LookupElement> T.withPriority(priority: Double?): T {
    val scopeMatched = this.scopeMatched
    if(priority == null && scopeMatched) return this
    var finalPriority = priority ?: 0.0
    if(!scopeMatched) finalPriority += ParadoxCompletionPriorities.scopeMismatchOffset
    this.priority = finalPriority
    return this
}

fun <T : LookupElement> T.withCompletionId(completionId: String = lookupString): T {
    this.completionId = completionId
    return this
}

fun <T : LookupElement> T.withPatchableIcon(icon: Icon?): T {
    this.patchableIcon = icon
    return this
}

fun <T : LookupElement> T.withPatchableTailText(tailText: String?): T {
    this.patchableTailText = tailText
    return this
}

fun LookupElementBuilder.withScopeMatched(scopeMatched: Boolean): LookupElementBuilder {
    this.scopeMatched = scopeMatched
    if(scopeMatched) return this
    return withItemTextForeground(JBColor.GRAY)
}

//for script expressions

fun <T : LookupElement> T.withForceInsertCurlyBraces(forceInsertCurlyBraces: Boolean): T {
    this.forceInsertCurlyBraces = forceInsertCurlyBraces
    return this
}

fun LookupElementBuilder.withDefinitionLocalizedNamesIfNecessary(element: ParadoxScriptDefinitionElement): LookupElementBuilder {
    if(getSettings().completion.completeByLocalizedName) {
        ProgressManager.checkCanceled()
        localizedNames = ParadoxDefinitionManager.getLocalizedNames(element)
    }
    return this
}

fun LookupElementBuilder.withModifierLocalizedNamesIfNecessary(modifierName: String, element: PsiElement): LookupElementBuilder {
    if(getSettings().completion.completeByLocalizedName) {
        ProgressManager.checkCanceled()
        localizedNames = ParadoxModifierManager.getModifierLocalizedNames(modifierName, element, element.project)
    }
    return this
}

fun LookupElementBuilder.forScriptExpression(context: ProcessingContext): LookupElement? {
    //check whether scope is matched again here
    if((!scopeMatched || !context.scopeMatched) && getSettings().completion.completeOnlyScopeIsMatched) return null
    
    val config = context.config
    val completeWithValue = getSettings().completion.completeWithValue
    val targetConfig = when {
        config is CwtPropertyConfig -> config
        config is CwtAliasConfig -> config.config
        config is CwtSingleAliasConfig -> config.config
        config is CwtInlineConfig -> config.config
        else -> null
    }
    val constantValue = when {
        completeWithValue -> targetConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }?.value
        else -> null
    }
    val insertCurlyBraces = when {
        forceInsertCurlyBraces -> true
        completeWithValue -> targetConfig?.isBlock ?: false
        else -> false
    }
    
    val isKey = context.isKey
    val isKeyOnly = context.contextElement is ParadoxScriptPropertyKey && isKey != false
    val isValueOnly = context.contextElement is ParadoxScriptValue && isKey != true
    val isBlock = targetConfig?.isBlock ?: false
    
    //排除重复项
    val completionId = when {
        isKeyOnly || isValueOnly -> lookupString
        constantValue != null -> "$lookupString = $constantValue"
        insertCurlyBraces -> "$lookupString = {...}"
        else -> lookupString
    }
    if(context.completionIds?.add(completionId) == false) return null
    
    var lookupElement = this
    
    val localizedNames = this.localizedNames
    if(localizedNames.isNotNullOrEmpty()) {
        lookupElement = lookupElement.withLookupStrings(localizedNames)
    }
    val patchableIcon = this.patchableIcon
    if(patchableIcon != null) {
        lookupElement = lookupElement.withIcon(getIconToUse(patchableIcon, config))
    }
    val patchableTailText = this.patchableTailText
    val tailText = buildString {
        if(!isKeyOnly && !isValueOnly) {
            if(constantValue != null) append(" = ").append(constantValue)
            if(insertCurlyBraces) append(" = {...}")
        }
        if(patchableTailText != null) append(patchableTailText)
    }
    lookupElement = lookupElement.withTailText(tailText, true)
    
    if(isKeyOnly || isValueOnly) { //key or value only
        lookupElement = lookupElement.withInsertHandler { c, _ -> applyKeyOrValueInsertHandler(c, context, isKey) }
    } else if(isKey == true) { // key with value
        lookupElement = lookupElement.withInsertHandler { c, _ -> applyKeyWithValueInsertHandler(c, context, isKey, constantValue, insertCurlyBraces) }
    }
    
    val extraElements = mutableListOf<LookupElement>()
    
    //进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
    if(isKey == true && !isKeyOnly && isBlock && config != null && getSettings().completion.completeWithClauseTemplate) {
        val entryConfigs = ParadoxExpressionManager.getEntryConfigs(config)
        if(entryConfigs.isNotEmpty()) {
            val extraTailText = buildString {
                append(" = { <generate via template> }")
                if(patchableTailText != null) append(patchableTailText)
            }
            val extraElement = lookupElement
                .withTailText(extraTailText)
                .withExpandClauseTemplateInsertHandler(context, entryConfigs)
                .withPriority(priority)
            extraElements.add(extraElement)
        }
    }
    
    lookupElement.extraLookupElements = extraElements
    return lookupElement
}

private fun getIconToUse(icon: Icon?, config: CwtConfig<*>?): Icon? {
    if(icon == null) return null
    when(config) {
        is CwtValueConfig -> {
            if(config.isTagConfig) return PlsIcons.Nodes.Tag
        }
        is CwtAliasConfig -> {
            val aliasConfig = config
            val type = aliasConfig.expression.type
            if(type !in CwtDataTypeGroups.ConstantLike) return icon
            val aliasName = aliasConfig.name
            return when {
                aliasName == "modifier" -> PlsIcons.Nodes.Modifier
                aliasName == "trigger" -> PlsIcons.Nodes.Trigger
                aliasName == "effect" -> PlsIcons.Nodes.Effect
                else -> icon
            }
        }
    }
    return icon
}

private fun applyKeyOrValueInsertHandler(c: InsertionContext, context: ProcessingContext, isKey: Boolean?) {
    skipOrInsertRightQuote(context, c.editor, isKey)
}

private fun applyKeyWithValueInsertHandler(c: InsertionContext, context: ProcessingContext, isKey: Boolean?, constantValue: String?, insertCurlyBraces: Boolean) {
    val editor = c.editor
    skipOrInsertRightQuote(context, c.editor, isKey)
    val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
    val text = buildString {
        append(if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "=")
        if(constantValue != null) append(constantValue)
        if(insertCurlyBraces) append(if(customSettings.SPACE_WITHIN_BRACES) "{  }" else "{}")
    }
    val length = when {
        insertCurlyBraces -> if(customSettings.SPACE_WITHIN_BRACES) text.length - 2 else text.length - 1
        else -> text.length
    }
    EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
}

@Suppress("UNUSED_PARAMETER", "SameParameterValue")
private fun applyValueInsertHandler(c: InsertionContext, context: ProcessingContext, insertCurlyBraces: Boolean) {
    if(!insertCurlyBraces) return
    val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
    val text = if(customSettings.SPACE_WITHIN_BRACES) "{  }" else "{}"
    val length = if(customSettings.SPACE_WITHIN_BRACES) text.length - 2 else text.length - 1
    EditorModificationUtil.insertStringAtCaret(c.editor, text, false, true, length)
}

private fun skipOrInsertRightQuote(context: ProcessingContext, editor: Editor, isKey: Boolean?) {
    //这里的isKey需要在创建LookupElement时就预先获取（之后可能会有所变更）
    //这里的isKey如果是null，表示已经填充的只是KEY或VALUE的其中一部分
    if(context.quoted) {
        val offset = editor.caretModel.offset
        val charsSequence = editor.document.charsSequence
        val rightQuoted = charsSequence.get(offset) == '"' && charsSequence.get(offset - 1) != '\\'
        if(rightQuoted) {
            //在必要时将光标移到右双引号之后
            if(isKey != null) editor.caretModel.moveToOffset(offset + 1)
        } else {
            //插入缺失的右双引号，且在必要时将光标移到右双引号之后
            EditorModificationUtil.insertStringAtCaret(editor, "\"", false, isKey != null)
        }
    }
}

@Suppress("UnstableApiUsage")
private fun LookupElementBuilder.withExpandClauseTemplateInsertHandler(context: ProcessingContext, entryConfigs: List<CwtMemberConfig<*>>): LookupElementBuilder {
    //如果补全位置所在的子句为空或者都不精确匹配，显示对话框时默认列出的属性/值应该有数种情况，因此这里需要传入entryConfigs
    //默认列出且仅允许选择直接的key为常量字符串的属性（忽略需要内联的情况）
    
    val file = context.parameters?.originalFile ?: return this
    val constantConfigGroupList = mutableListOf<Map<CwtDataExpression, List<CwtMemberConfig<*>>>>()
    val hasRemainList = mutableListOf<Boolean>()
    for(entry in entryConfigs) {
        val constantConfigGroup = entry.configs
            ?.filter { it is CwtPropertyConfig && it.expression.type == CwtDataTypes.Constant }
            ?.groupBy { it.expression }
            .orEmpty()
        if(constantConfigGroup.isEmpty()) continue //skip
        val configList = entry.configs
            ?.distinctBy { it.expression }
            .orEmpty()
        val hasRemain = constantConfigGroup.size != configList.size
        constantConfigGroupList.add(constantConfigGroup)
        hasRemainList.add(hasRemain)
    }
    if(constantConfigGroupList.isEmpty()) return this
    val config = context.config!!
    val propertyName = ParadoxExpressionManager.getEntryName(config)
    
    val isKey = context.isKey
    return this.withInsertHandler { c, _ ->
        if(isKey == true) {
            applyKeyWithValueInsertHandler(c, context, isKey, null, true)
        } else {
            applyValueInsertHandler(c, context, true)
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
            if(!dialog.showAndGet()) return@Runnable
            
            val descriptors = descriptorsContext.descriptorsInfo.resultDescriptors
            val hasRemain = descriptorsContext.descriptorsInfo.hasRemain
            
            val customSettings = CodeStyle.getCustomSettings(file, ParadoxScriptCodeStyleSettings::class.java)
            val multiline = descriptors.size > getSettings().completion.clauseTemplate.maxMemberCountInOneLine
            val around = customSettings.SPACE_AROUND_PROPERTY_SEPARATOR
            
            val documentManager = PsiDocumentManager.getInstance(project)
            val command = Runnable {
                documentManager.commitDocument(editor.document)
                val offset = editor.caretModel.offset
                val blockOffset = if(around) offset + 1 else offset
                
                val elementAtCaret = file.findElementAt(blockOffset)?.parent as ParadoxScriptValue
                val clauseText = buildString {
                    append("{")
                    if(multiline) append("\n")
                    descriptors.forEach {
                        when(it) {
                            is ValueDescriptor -> {
                                append(it.name.quoteIfNecessary())
                            }
                            is PropertyDescriptor -> {
                                append(it.name.quoteIfNecessary())
                                if(around) append(" ")
                                append(it.separator)
                                if(around) append(" ")
                                append(it.value.ifEmpty { "v" })
                            }
                        }
                        if(multiline) append("\n") else append(" ")
                    }
                    append("}")
                }
                val clauseElement = ParadoxScriptElementFactory.createValue(project, clauseText)
                val element = elementAtCaret.replace(clauseElement) as ParadoxScriptBlock
                documentManager.doPostponedOperationsAndUnblockDocument(editor.document) //提交文档更改
                
                val startAction = StartMarkAction.start(editor, project, PlsBundle.message("script.command.expandClauseTemplate.name"))
                val templateBuilder = TemplateBuilderFactory.getInstance().createTemplateBuilder(element)
                var i = 0
                element.processChild { e ->
                    if(e is ParadoxScriptProperty || e is ParadoxScriptValue) {
                        val descriptor = descriptors[i]
                        if(descriptor.editInTemplate) {
                            if(e is ParadoxScriptProperty && descriptor is PropertyDescriptor) {
                                val expression = TextExpression(descriptor.value.ifNotEmpty { it.quoteIfNecessary() })
                                templateBuilder.replaceElement(e.propertyValue!!, "${descriptor.name}_$i", expression, true)
                            }
                        }
                        i++
                    }
                    true
                }
                val textRange = element.textRange
                val caretMarker = editor.document.createRangeMarker(textRange.startOffset, textRange.endOffset)
                caretMarker.isGreedyToRight = true
                editor.caretModel.moveToOffset(textRange.startOffset)
                val template = templateBuilder.buildInlineTemplate()
                TemplateManager.getInstance(project).startTemplate(editor, template, TemplateEditingFinishedListener { _, _ ->
                    try {
                        //如果从句中没有其他可能的元素，将光标移到子句之后的位置
                        if(!hasRemain) {
                            editor.caretModel.moveToOffset(caretMarker.endOffset)
                        }
                        editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
                    } finally {
                        FinishMarkAction.finish(project, editor, startAction)
                    }
                })
            }
            WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("script.command.expandClauseTemplate.name"), null, command, file)
        }
    }
}

private fun getDescriptors(constantConfigGroup: Map<CwtDataExpression, List<CwtMemberConfig<*>>>): List<ElementDescriptor> {
    val descriptors = mutableListOf<ElementDescriptor>()
    for((expression, constantConfigs) in constantConfigGroup) {
        if(expression.isKey) {
            val name = expression.expressionString
            val constantValueExpressions = constantConfigs
                .mapNotNull { it.castOrNull<CwtPropertyConfig>()?.valueExpression?.takeIf { e -> e.type == CwtDataTypes.Constant } }
            val mustBeConstantValue = constantValueExpressions.size == constantConfigs.size
            val value = if(mustBeConstantValue) constantValueExpressions.first().expressionString else ""
            val constantValues = if(constantValueExpressions.isEmpty()) emptyList() else buildList {
                if(!mustBeConstantValue) add("")
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

fun CompletionResultSet.addBlockScriptExpressionElement(context: ProcessingContext) {
    val id = "{...}"
    if(context.completionIds?.add(id) == false) return
    val lookupElement = ParadoxLookupElements.blockLookupElement
    addElement(lookupElement)
    
    //进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
    if(getSettings().completion.completeWithClauseTemplate) {
        val config = context.config!!
        val entryConfigs = ParadoxExpressionManager.getEntryConfigs(config)
        if(entryConfigs.isNotEmpty()) {
            val extraTailText = "{ <generate via template> }"
            val extraLookupElement = LookupElementBuilder.create("")
                .withPresentableText(extraTailText)
                .withExpandClauseTemplateInsertHandler(context, entryConfigs)
            addElement(extraLookupElement.withPriority(ParadoxCompletionPriorities.keyword))
        }
    }
}
