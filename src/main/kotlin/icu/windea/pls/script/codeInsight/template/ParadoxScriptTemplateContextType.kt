package icu.windea.pls.script.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

abstract class ParadoxScriptTemplateContextType(presentableName: String): TemplateContextType(presentableName) {
	override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
		val file = templateActionContext.file
		if(!file.language.isKindOf(ParadoxScriptLanguage)) return false
		return doIsInContext(templateActionContext)
	}
	
	abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean
	
	class Base: ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType")) {
		override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
			return true
		}
	}
	
	class PropertyKey: ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.propertyKey")) {
		override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
			val file = templateActionContext.file
			val startOffset = templateActionContext.startOffset
			val endOffset = templateActionContext.endOffset
			val start = file.findElementAt(startOffset)
				?.siblings(forward = true, withSelf = true)
				?.find { it !is PsiWhiteSpace }
				?: return false
			val startElement = start
				.parents(false)
				.findIsInstance<ParadoxScriptPropertyKey>()
				?: return false
			if(endOffset <= 0) return true
			val end = file.findElementAt(endOffset - 1)
				?.siblings(forward = false, withSelf = true)
				?.find { it !is PsiWhiteSpace }
				?: return false
			val endElement = end
				.parents(false)
				.findIsInstance<ParadoxScriptPropertyKey>()
				?: return false
			return startElement === endElement
		}
	}
	
	class PropertyValue: ParadoxScriptTemplateContextType(PlsBundle.message("script.templateContextType.propertyValue")) {
		override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
			val file = templateActionContext.file
			val startOffset = templateActionContext.startOffset
			val endOffset = templateActionContext.endOffset
			val start = file.findElementAt(startOffset)
				?.siblings(forward = true, withSelf = true)
				?.find { it !is PsiWhiteSpace }
				?: return false
			val startElement = start
				.parents(false)
				.findIsInstance<ParadoxScriptValue>()
				?.takeIf { it.isPropertyValue() }
				?: return false
			val end = file.findElementAt(endOffset - 1)
				?.siblings(forward = false, withSelf = true)
				?.find { it !is PsiWhiteSpace }
				?: return false
			val endElement  = end
				.parents(false)
				.findIsInstance<ParadoxScriptValue>()
				?.takeIf { it.isPropertyValue() }
				?: return false
			return startElement === endElement
		}
	}
}