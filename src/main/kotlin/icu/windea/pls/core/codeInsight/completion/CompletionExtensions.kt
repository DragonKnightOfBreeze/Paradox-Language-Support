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
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
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
val ProcessingContext.prevScope get() = get(PlsCompletionKeys.prevScopeKey)
val ProcessingContext.isExpectedScopeMatched get() = get(PlsCompletionKeys.isExpectedScopeMatchedKey) ?: true
val ProcessingContext.scopeName get() = get(PlsCompletionKeys.scopeNameKey)
val ProcessingContext.scopeGroupName get() = get(PlsCompletionKeys.scopeGroupNameKey)
val ProcessingContext.isInt get() = get(PlsCompletionKeys.isIntKey) ?: false
val ProcessingContext.valueSetName get() = get(PlsCompletionKeys.valueSetName)

fun CompletionResultSet.addExpressionElement(
	lookupElement: LookupElement,
	context: ProcessingContext
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
		val lookupElement = LookupElementBuilder.create("").bold()
			.withPresentableText("{...}")
			.withInsertHandler { c, _ ->
				val editor = c.editor
				val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
				val text = if(customSettings.SPACE_WITHIN_BRACES) "{  }" else "{}"
				val length = if(customSettings.SPACE_WITHIN_BRACES) text.length - 2 else text.length - 1
				EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
			}
			.withPriority(PlsCompletionPriorities.keywordPriority)
		addElement(lookupElement)
	}
	
	//进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
	val completeWithClauseTemplate = getSettings().completion.completeWithClauseTemplate
	if(completeWithClauseTemplate) {
		val props = config?.castOrNull<CwtValueConfig>()?.properties
		if(props != null && props.isNotEmpty()) {
			val lookupElement = LookupElementBuilder.create("").bold()
			addScriptExpressionElementWithClauseTemplate(lookupElement, context, props, false) {
				withPriority(PlsCompletionPriorities.keywordPriority - 1) //under "{...}"
			}
		}
	}
}

fun CompletionResultSet.addScriptExpressionElement(
	element: PsiElement,
	lookupString: String,
	context: ProcessingContext,
	icon: Icon? = null,
	tailText: String? = null,
	typeText: String? = null,
	typeIcon: Icon? = null,
	builder: LookupElementBuilder.() -> LookupElement = { this }
) {
	val config = context.config
	
	val completeWithValue = getSettings().completion.completeWithValue
	val propertyConfig = when{
		config is CwtPropertyConfig -> config
		config is CwtAliasConfig -> config.config
		config is CwtSingleAliasConfig -> config.config
		else -> null
	}
	val constantValue = if(completeWithValue) propertyConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }?.value else null
	val insertCurlyBraces = if(completeWithValue) propertyConfig?.isBlock ?: false else false
	//这里ID不一定等同于lookupString
	val id = when {
		constantValue != null -> "$lookupString = $constantValue"
		insertCurlyBraces -> "$lookupString = {...}"
		else -> lookupString
	}
	//排除重复项
	if(context.completionIds?.add(id) == false) return
	
	var lookupElement = LookupElementBuilder.create(element, lookupString)
	if(icon != null) {
		lookupElement = lookupElement.withIcon(icon)
	}
	val finalTailText = buildString {
		if(constantValue != null) append(" = ").append(constantValue)
		if(insertCurlyBraces) append(" = {...}")
		if(tailText != null) append(tailText)
	}
	if(finalTailText.isNotEmpty()) {
		lookupElement = lookupElement.withTailText(finalTailText, true)
	}
	if(typeText != null) {
		lookupElement = lookupElement.withTypeText(typeText, typeIcon, true)
	}
	
	if(context.isKey != true || context.contextElement is ParadoxScriptPropertyKey) {
		val resultLookupElement = lookupElement.withInsertHandler { c, _ ->
			skipOrInsertRightQuote(context, c.editor)
		}
		addElement(resultLookupElement)
		return
	}
	
	if(context.isKey == true) {
		val resultLookupElement = lookupElement.withInsertHandler { c, _ ->
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
		addElement(resultLookupElement.builder())
	}
	
	//进行提示并在提示后插入子句内联模版（仅当子句中允许键为常量字符串的属性时才会提示）
	val completeWithClauseTemplate = getSettings().completion.completeWithClauseTemplate
	if(context.isKey == true && completeWithClauseTemplate) {
		val props = propertyConfig?.properties
		if(props != null && props.isNotEmpty()) {
			addScriptExpressionElementWithClauseTemplate(lookupElement, context, props, true, builder)
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

@Suppress("UnstableApiUsage")
fun CompletionResultSet.addScriptExpressionElementWithClauseTemplate(
	lookupElement: LookupElementBuilder,
	context: ProcessingContext,
	props: List<CwtPropertyConfig>,
	insertEq: Boolean,
	builder: LookupElementBuilder.() -> LookupElement = { this }
) {
	val file = context.originalFile ?: return
	val propsToCheck = props
		.distinctBy { it.key.lowercase() }
	val propsToInsert = props
		.filter { it.keyExpression.type == CwtDataTypes.ConstantKey }
		.distinctBy { it.key.lowercase() }
	if(propsToInsert.isEmpty()) return
	
	val tailText = if(insertEq) " = { <generate via template> }" else "{ <generate via template> }"
	val resultLookupElement = lookupElement
		.withTailText(tailText)
		.withInsertHandler { c, _ ->
			val editor = c.editor
			val project = file.project
			val customSettings = CodeStyle.getCustomSettings(file, ParadoxScriptCodeStyleSettings::class.java)
			
			val multiline = propsToInsert.size > getSettings().completion.maxExpressionCountInOneLine
			val hasRemain = propsToCheck.size != propsToInsert.size
			val separator = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
			val constantValuePropertyKeys = mutableSetOf<String>()
			
			val documentManager = PsiDocumentManager.getInstance(project)
			val command = Runnable {
				skipOrInsertRightQuote(context, editor)
				val text = if(insertEq) separator + "v" else "v"
				EditorModificationUtil.insertStringAtCaret(editor, text, false, true)
				documentManager.commitDocument(editor.document)
				val offset = editor.caretModel.offset
				
				val elementAtCaret = file.findElementAt(offset - 1)?.parent as ParadoxScriptString
				val clauseText = buildString {
					append("{")
					if(multiline) append("\n")
					for(prop in propsToInsert) {
						append(prop.key).append(separator)
						if(prop.valueExpression.type ==CwtDataTypes.Constant) {
							constantValuePropertyKeys.add(prop.key)
							append(prop.value)
						} else {
							append("v")
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
				element.processProperty { p ->
					val name = p.name
					if(name in constantValuePropertyKeys) return@processProperty true
					templateBuilder.replaceElement(p.propertyValue!!, name, TextExpression(""), true)
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
	addElement(resultLookupElement.builder())
}
