package icu.windea.pls.core.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.codeStyle.*
import javax.swing.*

object PlsLookupElementBuilder {
	@JvmStatic
	fun buildScriptExpressionLookupElement(
		element: PsiElement,
		lookupString: String,
		context: ProcessingContext,
		icon: Icon? = null,
		tailText: String? = null,
		typeText: String? = null,
		typeIcon: Icon? = null
	): LookupElementBuilder? = with(context) {
		val config = config
		
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
		if(context.completionIds?.add(id) == false) return null
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
		if(context.isKey == true) {
			lookupElement = lookupElement.withInsertHandler { c, _ ->
				val editor = c.editor
				val document = editor.document
				val chars = document.charsSequence
				val charsLength = chars.length
				val caretOffset = editor.caretModel.offset
				//得到光标之后的分隔符的位置
				var offset = caretOffset
				while(offset < charsLength && chars[offset].isWhitespace()) {
					offset++
				}
				//如果后面没有分隔符，则要自动插入等号，以及其他可能的要插入的文本
				//需要基于代码格式设置来决定要插入的文本的格式
				if(offset == charsLength || chars[offset] !in PlsConstants.separatorChars) {
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
			}
		}
		return lookupElement
	}
}
