package icu.windea.pls.core.editor

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
class ParadoxDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterInfo(element, originalElement)
			is ParadoxArgument -> getParameterInfo(element, originalElement)
			is ParadoxParameter -> getParameterInfo(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueInfo(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && CwtConfigHandler.isValueSetValue(config)) {
					return getValueSetValueInfo(element, originalElement)
				}
				null
			}
			else -> null
		}
	}
	
	private fun getParameterInfo(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxParameterElement ?: return null
		return getParameterInfo(resolved, originalElement)
	}
	
	private fun getParameterInfo(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		val name = element.name
		val definitionInfo = element.definitionName + ": " + element.definitionType
		return buildString {
			buildParameterDefinition(name, definitionInfo)
		}
	}
	
	private fun getValueSetValueInfo(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueInfo(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		val referenceElement = if(originalElement is LeafPsiElement) originalElement.parent else originalElement
		return buildString {
			when(referenceElement) {
				is ParadoxLocalisationStellarisNamePart -> buildStellarisNameFormatDefinition(name, valueSetNames)
				else -> buildValueSetValueDefinition(name, valueSetNames, configGroup)
			}
		}
	}
	
	override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterDoc(element, originalElement)
			is ParadoxArgument -> getParameterDoc(element, originalElement)
			is ParadoxParameter -> getParameterDoc(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueDoc(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && CwtConfigHandler.isValueSetValue(config)) {
					return getValueSetValueDoc(element, originalElement)
				}
				null
			}
			else -> null
		}
	}
	
	private fun getParameterDoc(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.reference?.resolve() as? ParadoxParameterElement ?: return null
		return getParameterInfo(resolved, originalElement)
	}
	
	private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		val name = element.name
		val definitionInfo = element.definitionName + ": " + element.definitionType
		return buildString {
			buildParameterDefinition(name, definitionInfo)
		}
	}
	
	private fun getValueSetValueDoc(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueDoc(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		val referenceElement = if(originalElement is LeafPsiElement) originalElement.parent else originalElement
		return buildString {
			when(referenceElement) {
				is ParadoxLocalisationStellarisNamePart -> buildStellarisNameFormatDefinition(name, valueSetNames)
				else -> buildValueSetValueDefinition(name, valueSetNames, configGroup)
			}
		}
	}
	
	private fun StringBuilder.buildParameterDefinition(name: String, definitionInfo: String?) {
		definition {
			//不加上文件信息
			//加上名字
			append(PlsDocBundle.message("name.script.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			if(definitionInfo != null) {
				append(" ")
				append(PlsDocBundle.message("ofDefinition", definitionInfo))
			}
		}
	}
	
	private fun StringBuilder.buildValueSetValueDefinition(name: String, valueSetNames: List<String>, configGroup: CwtConfigGroup) {
		definition {
			//不加上文件信息
			append(PlsDocBundle.message("prefix.valueSetValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			var appendSeparator = false
			for(valueSetName in valueSetNames) {
				if(appendSeparator) append(" | ") else appendSeparator = true
				val valueConfig = configGroup.values[valueSetName]
				if(valueConfig != null) {
					val gameType = configGroup.gameType
					val typeLink = "${gameType.id}/values/${valueSetName}"
					append(": ").appendCwtLink(valueSetName, typeLink)
				} else {
					append(": ").append(valueSetName)
				}
			}
		}
	}
	
	private fun StringBuilder.buildStellarisNameFormatDefinition(name: String, valueSetNames: List<String>) {
		definition {
			//不加上文件信息
			append(PlsDocBundle.message("name.localisation.stellarisNamePart")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			append(": ").append(valueSetNames.first())
		}
	}
}