package icu.windea.pls.core.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.command.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import javax.swing.*

val ProcessingContext.completionType get() = get(PlsCompletionKeys.completionTypeKey)
val ProcessingContext.completionIds get() = get(PlsCompletionKeys.completionIdsKey)
val ProcessingContext.contextElement get() = get(PlsCompletionKeys.contextElementKey)
val ProcessingContext.originalFile get() = get(PlsCompletionKeys.originalFileKey)
val ProcessingContext.quoted get() = get(PlsCompletionKeys.quotedKey)
val ProcessingContext.rightQuoted get() = get(PlsCompletionKeys.rightQuotedKey)
val ProcessingContext.offsetInParent get() = get(PlsCompletionKeys.offsetInParentKey)
val ProcessingContext.keyword get() = get(PlsCompletionKeys.keywordKey)
val ProcessingContext.isKey: Boolean? get() = get(PlsCompletionKeys.isKeyKey)
val ProcessingContext.config get() = get(PlsCompletionKeys.configKey)
val ProcessingContext.configs get() = get(PlsCompletionKeys.configsKey)
val ProcessingContext.configGroup get() = get(PlsCompletionKeys.configGroupKey)
val ProcessingContext.scopeContext get() = get(PlsCompletionKeys.scopeContextKey)
val ProcessingContext.prevScope get() = get(PlsCompletionKeys.prevScopeKey)
val ProcessingContext.scopeName get() = get(PlsCompletionKeys.scopeNameKey)
val ProcessingContext.scopeGroupName get() = get(PlsCompletionKeys.scopeGroupNameKey)
val ProcessingContext.isInt get() = get(PlsCompletionKeys.isIntKey) ?: false

fun PsiElement.getKeyword(offsetInParent: Int): String {
	return text.substring(0, offsetInParent).unquote()
}

fun PsiElement.getFullKeyword(offsetInParent: Int): String {
	return (text.substring(0, offsetInParent) + text.substring(offsetInParent + PlsConstants.dummyIdentifier.length)).unquote()
}

fun LookupElementBuilder.withScopeMatched(scopeMatched: Boolean) : LookupElementBuilder {
	if(scopeMatched) return this
	putUserData(PlsKeys.scopeMismatchedKey, true)
	return withItemTextForeground(JBColor.GRAY)
}

fun LookupElement.withPriority(priority: Double?, offset: Double = 0.0): LookupElement {
	val scopeMatched = getUserData(PlsKeys.scopeMismatchedKey) != true
	if(priority == null && scopeMatched) return this
	val finalPriority =  (priority ?: 0.0) + offset + (if(scopeMatched) 0 else PlsCompletionPriorities.scopeMismatchOffset)
	return PrioritizedLookupElement.withPriority(this, finalPriority)
}

fun LookupElement.withExplicitProximity(explicitProximity: Int): LookupElement {
	return PrioritizedLookupElement.withExplicitProximity(this, explicitProximity)
}

fun CompletionResultSet.addExpressionElement(
	context: ProcessingContext,
	lookupElement: LookupElement
) {
	val id = lookupElement.lookupString
	if(context.completionIds?.add(id) == false) return
	addElement(lookupElement)
}

fun CompletionResultSet.addBlockElement(context: ProcessingContext) {
	val id = "{...}"
	//排除重复项
	if(context.completionIds?.add(id) == false) return
	
	val config = context.config
	
	run {
		val lookupElement = PlsLookupElements.blockLookupElement
		addExpressionElement(context, lookupElement)
	}
	
	//进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
	val completeWithClauseTemplate = getSettings().completion.completeWithClauseTemplate
	if(completeWithClauseTemplate) {
		val targetConfig = config?.castOrNull<CwtValueConfig>()
		if(targetConfig != null && !targetConfig.configs.isNullOrEmpty()) {
			val tailText1 = "{ <generate via template> }"
			val lookupElement1 = LookupElementBuilder.create("")
				.withPresentableText(tailText1)
			addScriptExpressionElementWithClauseTemplate(context, lookupElement1, targetConfig) {
				val offset = if(getSettings().completion.preferCompleteWithClauseTemplate) 1.0 else -1.0 
				withPriority(PlsCompletionPriorities.keywordPriority, offset) //under "{...}"
			}
		}
	}
}

fun CompletionResultSet.addScriptExpressionElement(
	context: ProcessingContext,
	builder: ParadoxScriptExpressionLookupElementBuilder
) = with(builder) {
	//should be filtered out before, check again here
	if(!builder.scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return
	
	val config = context.config
	
	val completeWithValue = getSettings().completion.completeWithValue
	val propertyConfig = when {
		config is CwtPropertyConfig -> config
		config is CwtAliasConfig -> config.config
		config is CwtSingleAliasConfig -> config.config
		else -> null
	}
	val constantValue = when {
		completeWithValue -> propertyConfig?.valueExpression?.takeIf { it.type == CwtDataType.Constant }?.value
		else -> null
	}
	val insertCurlyBraces = when {
		forceInsertCurlyBraces -> true
		completeWithValue -> propertyConfig?.isBlock ?: false
		else -> false
	}
	//这里ID不一定等同于lookupString
	val id = when {
		constantValue != null -> "$lookupString = $constantValue"
		insertCurlyBraces -> "$lookupString = {...}"
		else -> lookupString
	}
	//排除重复项
	if(context.completionIds?.add(id) == false) return
	
	val isKeyOrValueOnly = context.contextElement is ParadoxScriptPropertyKey || context.isKey != true
	val isKey = context.isKey == true
	var lookupElement = when {
		element != null -> LookupElementBuilder.create(element, lookupString)
		else -> LookupElementBuilder.create(lookupString)
	}
	if(!scopeMatched) {
		lookupElement.putUserData(PlsKeys.scopeMismatchedKey, true)
	}
	if(bold) {
		lookupElement = lookupElement.bold()
	}
	if(caseSensitive) {
		lookupElement = lookupElement.withCaseSensitivity(true)
	}
	if(icon != null) {
		lookupElement = lookupElement.withIcon(icon)
	}
	if(presentableText != null) {
		lookupElement = lookupElement.withPresentableText(presentableText!!)
	}
	val finalTailText = buildString {
		if(!isKeyOrValueOnly) {
			if(constantValue != null) append(" = ").append(constantValue)
			if(insertCurlyBraces) append(" = {...}")
		}
		if(tailText != null) append(tailText)
	}
	if(finalTailText.isNotEmpty()) {
		lookupElement = lookupElement.withTailText(finalTailText, true)
	}
	if(typeText != null) {
		lookupElement = lookupElement.withTypeText(typeText, typeIcon, true)
	}
	if(!scopeMatched) {
		lookupElement = lookupElement.withItemTextForeground(JBColor.GRAY)
	}
	
	if(isKeyOrValueOnly) {
		val resultLookupElement = lookupElement.withInsertHandler { c, _ ->
			applyKeyOrValueInsertHandler(context, c)
		}.withPriority(priority)
		addElement(resultLookupElement)
		return
	}
	
	if(isKey) {
		val resultLookupElement = lookupElement.withInsertHandler { c, _ ->
			applyKeyAndValueInsertHandler(c, context, constantValue, insertCurlyBraces)
		}.withPriority(priority)
		addElement(resultLookupElement)
	}
	
	//进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
	val completeWithClauseTemplate = getSettings().completion.completeWithClauseTemplate
	if(isKey && completeWithClauseTemplate) {
		val targetConfig = propertyConfig
		if(targetConfig != null && !targetConfig.configs.isNullOrEmpty()) {
			val tailText1 = buildString {
				append(" = { <generate via template> }")
				if(tailText != null) append(tailText)
			}
			val lookupElement1 = lookupElement.withTailText(tailText1)
			addScriptExpressionElementWithClauseTemplate(context, lookupElement1, targetConfig) {
				val offset = if(getSettings().completion.preferCompleteWithClauseTemplate) 1.0 else -1.0
				withPriority(priority, offset)
			}
		}
	}
}

private fun skipOrInsertRightQuote(context: ProcessingContext, editor: Editor) {
	if(context.quoted) {
		val offset = editor.caretModel.offset
		val charsSequence = editor.document.charsSequence
		if(charsSequence.get(offset) == '"' && charsSequence.get(offset - 1) != '\\') {
			//移到右边的双引号之后
			editor.caretModel.moveToOffset(offset + 1)
		} else {
			//插入缺失的右边的双引号
			EditorModificationUtil.insertStringAtCaret(editor, "\"")
		}
	}
}

private fun applyKeyOrValueInsertHandler(context: ProcessingContext, c: InsertionContext) {
	skipOrInsertRightQuote(context, c.editor)
}

@Suppress("UNUSED_PARAMETER")
private fun applyValueInsertHandler(c: InsertionContext, context: ProcessingContext, insertCurlyBraces: Boolean) {
	if(!insertCurlyBraces) return
	val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
	val text = if(customSettings.SPACE_WITHIN_BRACES) "{  }" else "{}"
	val length = if(customSettings.SPACE_WITHIN_BRACES) text.length - 2 else text.length - 1
	EditorModificationUtil.insertStringAtCaret(c.editor, text, false, true, length)
}

private fun applyKeyAndValueInsertHandler(c: InsertionContext, context: ProcessingContext, constantValue: String?, insertCurlyBraces: Boolean) {
	val editor = c.editor
	skipOrInsertRightQuote(context, c.editor)
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

@Suppress("UnstableApiUsage", "DialogTitleCapitalization")
fun CompletionResultSet.addScriptExpressionElementWithClauseTemplate(
	context: ProcessingContext,
	builder: LookupElementBuilder,
	targetConfig: CwtDataConfig<*>,
	callback: LookupElementBuilder.() -> LookupElement = { this }
) {
	val file = context.originalFile ?: return
	val configs = targetConfig.configs.orEmpty()
	val configList = configs
		.distinctBy { it.expression }
	val constantConfigGroup = configs
		.filter { it.expression.type == CwtDataType.ConstantKey || it.expression.type == CwtDataType.Constant }
		.groupBy { it.expression }
	if(constantConfigGroup.isEmpty()) return
	
	val resultLookupElement = builder.withInsertHandler { c, _ ->
		when(targetConfig) {
			is CwtPropertyConfig -> applyKeyAndValueInsertHandler(c, context, null, true)
			is CwtValueConfig -> applyValueInsertHandler(c, context, true)
		}
		
		c.laterRunnable = Runnable {
			val editor = c.editor
			val project = file.project
			
			val allDescriptors = getDescriptors(constantConfigGroup)
			val propertyName = when(targetConfig) {
				is CwtValueConfig -> targetConfig.propertyConfig?.key
				is CwtPropertyConfig -> targetConfig.key
			}
			val dialog = ExpandClauseTemplateDialog(project, editor, propertyName, allDescriptors)
			if(!dialog.showAndGet()) return@Runnable
			val descriptors = dialog.context.resultDescriptors
			
			val hasRemain = configList.size != constantConfigGroup.size
			val customSettings = CodeStyle.getCustomSettings(file, ParadoxScriptCodeStyleSettings::class.java)
			val multiline = descriptors.size > getSettings().completion.maxExpressionCountInOneLine
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
				
				val startAction = StartMarkAction.start(editor, project, "script.command.expandClauseTemplate.name")
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
	addElement(resultLookupElement.callback())
}

private fun getDescriptors(constantConfigGroup: Map<CwtDataExpression, List<CwtDataConfig<*>>>): List<ElementDescriptor> {
	val descriptors = mutableListOf<ElementDescriptor>()
	for((expression, constantConfigs) in constantConfigGroup) {
		when(expression) {
			is CwtValueExpression -> {
				val descriptor = ValueDescriptor(name = expression.expressionString)
				descriptors.add(descriptor)
			}
			is CwtKeyExpression -> {
				val name = expression.expressionString
				val constantValueExpressions = constantConfigs
					.mapNotNull { it.castOrNull<CwtPropertyConfig>()?.valueExpression?.takeIf { e -> e.type == CwtDataType.Constant } }
				val mustBeConstantValue = constantValueExpressions.size == constantConfigs.size
				val value = if(mustBeConstantValue) constantValueExpressions.first().expressionString else ""
				val constantValues = if(constantValueExpressions.isEmpty()) emptyList() else buildList {
					if(!mustBeConstantValue) add("")
					constantValueExpressions.forEach { add(it.expressionString) }
				}
				val descriptor = PropertyDescriptor(name = name, value = value, constantValues = constantValues)
				descriptors.add(descriptor)
			}
		}
	}
	return descriptors
}
