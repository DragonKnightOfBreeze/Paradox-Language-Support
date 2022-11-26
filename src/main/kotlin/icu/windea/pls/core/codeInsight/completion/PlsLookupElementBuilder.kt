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
	): LookupElementBuilder? {
		val onlyConfig = context.configs?.singleOrNull()?.castOrNull<CwtPropertyConfig>()
		val onlyValue = onlyConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }
		//这里ID不一定等同于lookupString
		val id = lookupString
		//排除重复项
		if(context.completionIds?.add(id) == false) return null
		var lookupElement = LookupElementBuilder.create(element, lookupString)
		if(icon != null) {
			lookupElement = lookupElement.withIcon(icon)
		}
		val finalTailText = buildString {
			if(onlyValue != null) append(" = ").append(onlyValue)
			if(tailText != null) append(tailText)
		}
		if(finalTailText.isNotEmpty()) {
			lookupElement = lookupElement.withTailText(finalTailText, true)
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
				//如果后面没有分隔符，则要自动插入等号，并且根据代码格式设置来判断是否加上等号周围的空格
				//如果对应的value是唯一确定的，则还要自动插入这个值
				if(offset == charsLength || chars[offset] !in PlsConstants.separatorChars) {
					val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
					val textToInsert = buildString {
						val separator = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
						append(separator)
						if(onlyValue != null) append(onlyValue)
					}
					EditorModificationUtil.insertStringAtCaret(editor, textToInsert)
				}
			}
		}
		if(typeText != null) {
			lookupElement = lookupElement.withTypeText(typeText, typeIcon, true)
		}
		return lookupElement
	}
}
